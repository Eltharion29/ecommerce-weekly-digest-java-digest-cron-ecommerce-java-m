package dev.infrai.digest;

import dev.infrai.digest.config.DigestConfiguration;
import dev.infrai.digest.domain.CustomerOrder;
import dev.infrai.digest.infra.InfraiCronClient;
import dev.infrai.digest.service.WeeklyDigestService;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static dev.infrai.digest.domain.CustomerOrder.CheckoutStatus.CAPTURED;
import static dev.infrai.digest.domain.CustomerOrder.FulfillmentStatus.SHIPPED;
import static dev.infrai.digest.domain.CustomerOrder.ReceiptStatus.ISSUED;

public final class DigestCutover {
    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "preview".equals(args[0])) {
            preview();
            return;
        }
        if ("register".equals(args[0])) {
            DigestConfiguration config = DigestConfiguration.fromEnvironment();
            String jobId = new InfraiCronClient(HttpClient.newHttpClient(), config).createWeeklyDigestJob();
            System.out.println("Scheduled weekly digest job: " + jobId);
            return;
        }
        throw new IllegalArgumentException("Use preview or register");
    }

    private static void preview() {
        Instant now = Instant.now();
        CustomerOrder ready = new CustomerOrder(
                "order-1042", "customer-88", CAPTURED, SHIPPED, ISSUED, true, now.minusSeconds(3600));
        var digest = new WeeklyDigestService(Clock.systemUTC()).prepare(List.of(ready));
        System.out.println("Digest window: " + digest.windowStart() + " to " + digest.windowEnd());
        digest.entries().forEach(entry -> System.out.println(
                entry.orderId() + " -> " + entry.customerId() + " [" + entry.fulfillmentState() + "]"));
    }
}
