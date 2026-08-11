package de.ccq.resourcehub.entity;

/**
 * Enumeration of possible booking statuses.
 */
public enum BookingStatus {
    /**
     * Booking request has been created and is pending approval.
     */
    PENDING,

    /**
     * Booking request has been approved by a resource manager.
     */
    APPROVED,

    /**
     * Booking request has been rejected by a resource manager.
     */
    REJECTED,

    /**
     * Booking has been canceled by the user.
     */
    CANCELED,

    /**
     * Booking has been completed (resource returned, if applicable).
     */
    COMPLETED
}