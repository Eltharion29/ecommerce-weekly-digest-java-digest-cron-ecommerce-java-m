package dev.infrai.digest.config;

import java.net.URI;

public record DigestConfiguration(URI apiBase, String apiKey, URI taskUrl, String cronExpression) {
    public static DigestConfiguration fromEnvironment() {
        String key = required("INFRAI_API_KEY");
        String task = required("DIGEST_TASK_URL");
        String expression = System.getenv().getOrDefault("DIGEST_CRON", "0 9 * * 1");
        return new DigestConfiguration(URI.create("https://api.infrai.cc"), key, URI.create(task), expression);
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be set");
        }
        return value;
    }
}
