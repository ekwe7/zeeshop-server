package com.ekwe_hub.zeeshopserver.supplierpurchase.entity;

/**
 * Lifecycle of a Purchase, driven entirely by receiving stock (see
 * PurchaseService.receiveStock):
 *
 *   PENDING            -> created, nothing received yet
 *   PARTIALLY_RECEIVED -> some but not all ordered quantity received
 *   COMPLETED          -> every item fully received; inventory and the
 *                         supplier's balance have been updated
 *   CANCELLED          -> abandoned before anything was received
 */
public enum PurchaseStatus {
    PENDING,
    PARTIALLY_RECEIVED,
    COMPLETED,
    CANCELLED
}
