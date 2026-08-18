package dev.infrai.digest.domain;

import java.time.Instant;

public record CustomerOrder(
        String orderId,
        String customerId,
        CheckoutStatus checkout,
        FulfillmentStatus fulfillment,
        ReceiptStatus receipt,
        boolean customerUpdatesEnabled,
        Instant updatedAt) {

    public enum CheckoutStatus { PENDING, CAPTURED, REFUNDED }
    public enum FulfillmentStatus { UNFULFILLED, SHIPPED, DELIVERED, CANCELLED }
    public enum ReceiptStatus { PENDING, ISSUED }
}
