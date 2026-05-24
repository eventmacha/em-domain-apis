package com.eventmacha.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/**
 * Request body for POST /publish/save-draft.
 */
public class SaveDraftRequest {

    @NotBlank(message = "orderId is required")
    private String orderId;

    /** Site configuration as a raw JSON object (will be serialized to a String in DynamoDB). */
    private Map<String, Object> siteConfig;

    /** English content as a raw JSON object. */
    private Map<String, Object> contentEn;

    /** Localized content as a raw JSON object. */
    private Map<String, Object> contentLocalized;

    /** List of image keys / URLs for the draft. */
    private List<String> images;

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Map<String, Object> getSiteConfig() { return siteConfig; }
    public void setSiteConfig(Map<String, Object> siteConfig) { this.siteConfig = siteConfig; }

    public Map<String, Object> getContentEn() { return contentEn; }
    public void setContentEn(Map<String, Object> contentEn) { this.contentEn = contentEn; }

    public Map<String, Object> getContentLocalized() { return contentLocalized; }
    public void setContentLocalized(Map<String, Object> contentLocalized) { this.contentLocalized = contentLocalized; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
