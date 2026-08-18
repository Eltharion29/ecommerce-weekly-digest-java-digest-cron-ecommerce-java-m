package dev.infrai.digest.infra;

import dev.infrai.digest.config.DigestConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class InfraiCronClient {
    private static final String CREATE_PATH = "/v1/cron/create";
    private final HttpClient http;
    private final DigestConfiguration config;

    public InfraiCronClient(HttpClient http, DigestConfiguration config) {
        this.http = http;
        this.config = config;
    }

    public String createWeeklyDigestJob() throws IOException, InterruptedException {
        // POST /v1/cron/create registers the weekly task URL.
        String body = "{\"cron_expr\":\"" + escape(config.cronExpression())
                + "\",\"task\":\"" + escape(config.taskUrl().toString()) + "\"}";
        String idempotencyKey = "weekly-digest:" + config.cronExpression() + ":" + config.taskUrl();

        for (int attempt = 0; attempt < 4; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(config.apiBase().resolve(CREATE_PATH))
                    .header("Authorization", "Bearer " + config.apiKey())
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", idempotencyKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> envelope = Json.parseObject(response.body());

            if (response.statusCode() == 429 && attempt < 3) {
                Thread.sleep(retryDelay(response, attempt).toMillis());
                continue;
            }
            if (!Boolean.TRUE.equals(envelope.get("ok"))) {
                throw envelopeError(envelope, response.statusCode());
            }
            if (response.statusCode() >= 500) {
                throw new IOException("Infrai transport status " + response.statusCode());
            }
            Map<String, Object> data = object(envelope.get("data"));
            return String.valueOf(data.get("job_id"));
        }
        throw new IllegalStateException("Retry loop exhausted");
    }

    private static Duration retryDelay(HttpResponse<?> response, int attempt) {
        String retryAfter = response.headers().firstValue("Retry-After").orElse("");
        try {
            return Duration.ofSeconds(Math.max(1, Long.parseLong(retryAfter)));
        } catch (NumberFormatException ignored) {
            return Duration.ofSeconds(1L << attempt);
        }
    }

    private static InfraiException envelopeError(Map<String, Object> envelope, int status) {
        Map<String, Object> error = object(envelope.get("error"));
        return new InfraiException(String.valueOf(error.getOrDefault("code", "INFRAI_REJECTED")), error, status);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value) {
        if (value instanceof Map<?, ?> map) return (Map<String, Object>) map;
        return Map.of();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
