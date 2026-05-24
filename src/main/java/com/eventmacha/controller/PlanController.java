package com.eventmacha.controller;

import com.eventmacha.common.ApiResponse;
import com.eventmacha.common.PaginatedResponse;
import com.eventmacha.dto.response.PlanResponse;
import com.eventmacha.enums.UserType;
import com.eventmacha.service.PlanService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Public pricing plan endpoint.
 */
@Path("/plans")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Plans", description = "Public pricing plan catalogue")
public class PlanController {

    private static final Logger LOG = Logger.getLogger(PlanController.class);

    @Inject
    PlanService planService;

    /**
     * GET /plans
     * Returns all active rate cards and their plan details.
     * This endpoint is open (no JWT required).
     */
    @GET
    @Operation(summary = "Get Pricing Plans",
            description = "Returns all active rate cards with plan details. " +
                    "Filter by userType to get plans relevant to a specific audience.")
    @APIResponse(responseCode = "200", description = "List of active plans")
    public Response getPlans(
            @QueryParam("userType")
            @Parameter(description = "Filter by userType: CUSTOMER, AGENT, ADMIN")
            String userType) {

        LOG.debugf("GET /plans userType=%s", userType);
        List<PlanResponse> plans = planService.getActivePlans(UserType.valueOf(userType));
        return Response.ok(ApiResponse.ok(PaginatedResponse.of(plans))).build();
    }
}
