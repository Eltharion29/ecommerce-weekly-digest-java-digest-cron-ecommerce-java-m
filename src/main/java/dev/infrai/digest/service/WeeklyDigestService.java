package dev.infrai.digest.service;

import dev.infrai.digest.domain.CustomerOrder;
import dev.infrai.digest.domain.WeeklyDigest;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import static dev.infrai.digest.domain.CustomerOrder.CheckoutStatus.CAPTURED;
import static dev.infrai.digest.domain.CustomerOrder.FulfillmentStatus.DELIVERED;
import static dev.infrai.digest.domain.CustomerOrder.FulfillmentStatus.SHIPPED;
import static dev.infrai.digest.domain.CustomerOrder.ReceiptStatus.ISSUED;

public final class WeeklyDigestService {
    private final Clock clock;

    public WeeklyDigestService(Clock clock) {
        this.clock = clock;
    }

    public WeeklyDigest prepare(List<CustomerOrder> orders) {
        Instant end = clock.instant();
        Instant start = end.minus(Duration.ofDays(7));
        List<WeeklyDigest.Entry> entries = orders.stream()
                .filter(order -> !order.updatedAt().isBefore(start) && order.updatedAt().isBefore(end))
                .filter(order -> order.checkout() == CAPTURED)
                .filter(order -> order.fulfillment() == SHIPPED || order.fulfillment() == DELIVERED)
                .filter(order -> order.receipt() == ISSUED)
                .filter(CustomerOrder::customerUpdatesEnabled)
                .sorted(Comparator.comparing(CustomerOrder::orderId))
                .map(order -> new WeeklyDigest.Entry(
                        order.orderId(), order.customerId(), order.fulfillment().name()))
                .toList();
        return new WeeklyDigest(start, end, entries);
    }
}
