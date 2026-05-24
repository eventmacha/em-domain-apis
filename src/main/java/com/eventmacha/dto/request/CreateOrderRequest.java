package com.eventmacha.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Request body for POST /orders.
 */
public class CreateOrderRequest {

    @NotBlank(message = "rateCardId is required")
    private String rateCardId;

    @NotBlank(message = "planType is required")
    private String planType;

    private String eventId;

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getRateCardId() { return rateCardId; }
    public void setRateCardId(String rateCardId) { this.rateCardId = rateCardId; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
}
