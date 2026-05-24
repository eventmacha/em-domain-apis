package com.eventmacha.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /payments – initiates a Razorpay order and records the payment intent.
 */
public class CreatePaymentRequest {

    @NotBlank(message = "orderId is required")
    private String orderId;

    /** Optional Razorpay payment method preference (e.g. "upi", "card"). */
    private String paymentMethod;

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
