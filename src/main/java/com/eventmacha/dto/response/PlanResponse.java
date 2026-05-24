package com.eventmacha.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Plan response DTO – represents a single RateCard + its associated plans.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanResponse {

    private String rateCardId;
    private String userType;
    private String rateCardType;
    private String currency;
    private Boolean active;
    private Long effectiveFrom;
    private Long effectiveTo;
    private List<PlanDetail> plans;

    // ── Nested ────────────────────────────────────────────────────────────────

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PlanDetail {
        private String planType;
        private String displayName;
        private String description;
        private Double basePrice;
        private Double discount;
        private Double taxPercent;
        private Double finalPrice;
        private Integer durationDays;
        private Boolean active;

        public String getPlanType() { return planType; }
        public void setPlanType(String planType) { this.planType = planType; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getBasePrice() { return basePrice; }
        public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }
        public Double getDiscount() { return discount; }
        public void setDiscount(Double discount) { this.discount = discount; }
        public Double getTaxPercent() { return taxPercent; }
        public void setTaxPercent(Double taxPercent) { this.taxPercent = taxPercent; }
        public Double getFinalPrice() { return finalPrice; }
        public void setFinalPrice(Double finalPrice) { this.finalPrice = finalPrice; }
        public Integer getDurationDays() { return durationDays; }
        public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getRateCardId() { return rateCardId; }
    public void setRateCardId(String rateCardId) { this.rateCardId = rateCardId; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getRateCardType() { return rateCardType; }
    public void setRateCardType(String rateCardType) { this.rateCardType = rateCardType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Long getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(Long effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public Long getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(Long effectiveTo) { this.effectiveTo = effectiveTo; }

    public List<PlanDetail> getPlans() { return plans; }
    public void setPlans(List<PlanDetail> plans) { this.plans = plans; }
}
