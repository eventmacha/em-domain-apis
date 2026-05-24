package com.eventmacha.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /publish/live.
 */
public class PublishLiveRequest {

    @NotBlank(message = "orderId is required")
    private String orderId;

    /** Optional reason for publish (e.g. "Initial publish" or "Content update"). */
    private String changeReason;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
}
