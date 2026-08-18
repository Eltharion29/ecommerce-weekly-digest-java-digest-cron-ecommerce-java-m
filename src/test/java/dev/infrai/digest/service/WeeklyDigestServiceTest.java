package dev.infrai.digest.service;

import dev.infrai.digest.domain.CustomerOrder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static dev.infrai.digest.domain.CustomerOrder.CheckoutStatus.CAPTURED;
import static dev.infrai.digest.domain.CustomerOrder.FulfillmentStatus.SHIPPED;
import static dev.infrai.digest.domain.CustomerOrder.ReceiptStatus.ISSUED;

public final class WeeklyDigestServiceTest {
    public static void main(String[] args) {
        Instant now = Instant.parse("2026-08-17T09:00:00Z");
        WeeklyDigestService service = new WeeklyDigestService(Clock.fixed(now, ZoneOffset.UTC));
        List<CustomerOrder> orders = List.of(
                order("ready", CAPTURED, ISSUED, true, now.minusSeconds(60)),
                order("checkout-open", CustomerOrder.CheckoutStatus.PENDING, ISSUED, true, now.minusSeconds(60)),
                order("receipt-open", CAPTURED, CustomerOrder.ReceiptStatus.PENDING, true, now.minusSeconds(60)),
                order("updates-off", CAPTURED, ISSUED, false, now.minusSeconds(60)),
                order("too-old", CAPTURED, ISSUED, true, now.minusSeconds(8 * 86400L)));

        var digest = service.prepare(orders);
        if (digest.entries().size() != 1 || !digest.entries().get(0).orderId().equals("ready")) {
            throw new AssertionError("Expected only the settled, shipped, receipted, opted-in order");
        }
        System.out.println("PASS: ready is the only order selected");
    }

    private static CustomerOrder order(String id, CustomerOrder.CheckoutStatus checkout,
                                       CustomerOrder.ReceiptStatus receipt, boolean updates, Instant changed) {
        return new CustomerOrder(id, "customer-1", checkout, SHIPPED, receipt, updates, changed);
    }
}
