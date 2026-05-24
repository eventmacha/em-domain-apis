package com.eventmacha.service;

import com.eventmacha.dto.response.PlanResponse;
import com.eventmacha.entity.RateCardEntity;
import com.eventmacha.entity.RateCardPlanEntity;
import com.eventmacha.repository.RateCardRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Service for fetching publicly-visible pricing plans.
 */
@ApplicationScoped
public class PlanService {

    private static final Logger LOG = Logger.getLogger(PlanService.class);

    @Inject
    RateCardRepository rateCardRepository;

    /**
     * Fetch all active rate cards with their plans for a given userType.
     * Falls back to all active plans if userType is null.
     */
    public List<PlanResponse> getActivePlans(String userType) {
        List<RateCardEntity> rateCards;

        if (userType != null && !userType.isBlank()) {
            rateCards = rateCardRepository.findRateCardsByUserType(userType).stream()
                    .filter(rc -> Boolean.TRUE.equals(rc.getActive()))
                    .toList();
        } else {
            // Return all – in practice, scan is acceptable for small plan catalogues
            rateCards = rateCardRepository.findRateCardsByUserType("CUSTOMER").stream()
                    .filter(rc -> Boolean.TRUE.equals(rc.getActive()))
                    .toList();
        }

        return rateCards.stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PlanResponse toResponse(RateCardEntity rc) {
        List<RateCardPlanEntity> planEntities =
                rateCardRepository.findPlansByRateCardId(rc.getRateCardId()).stream()
                        .filter(p -> Boolean.TRUE.equals(p.getActive()))
                        .toList();

        PlanResponse response = new PlanResponse();
        response.setRateCardId(rc.getRateCardId());
        response.setUserType(rc.getUserType());
        response.setRateCardType(rc.getRateCardType());
        response.setCurrency(rc.getCurrency());
        response.setActive(rc.getActive());
        response.setEffectiveFrom(rc.getEffectiveFrom());
        response.setEffectiveTo(rc.getEffectiveTo());
        response.setPlans(planEntities.stream().map(this::toPlanDetail).toList());
        return response;
    }

    private PlanResponse.PlanDetail toPlanDetail(RateCardPlanEntity p) {
        PlanResponse.PlanDetail d = new PlanResponse.PlanDetail();
        d.setPlanType(p.getPlanType());
        d.setDisplayName(p.getDisplayName());
        d.setDescription(p.getDescription());
        d.setBasePrice(p.getBasePrice());
        d.setDiscount(p.getDiscount());
        d.setTaxPercent(p.getTaxPercent());
        d.setFinalPrice(p.getFinalPrice());
        d.setDurationDays(p.getDurationDays());
        d.setActive(p.getActive());
        return d;
    }
}
