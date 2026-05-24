package com.eventmacha.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /publish/preview.
 */
public class PreviewRequest {

    @NotBlank(message = "orderId is required")
    private String orderId;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}
