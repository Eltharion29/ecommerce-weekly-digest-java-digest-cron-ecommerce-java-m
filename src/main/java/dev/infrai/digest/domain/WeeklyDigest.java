package dev.infrai.digest.domain;

import java.time.Instant;
import java.util.List;

public record WeeklyDigest(Instant windowStart, Instant windowEnd, List<Entry> entries) {
    public record Entry(String orderId, String customerId, String fulfillmentState) {}
}
