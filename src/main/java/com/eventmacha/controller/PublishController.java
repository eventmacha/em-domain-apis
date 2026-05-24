package com.eventmacha.controller;

import com.eventmacha.common.ApiResponse;
import com.eventmacha.common.PaginatedResponse;
import com.eventmacha.dto.request.PublishLiveRequest;
import com.eventmacha.dto.request.SaveDraftRequest;
import com.eventmacha.dto.response.PublishHistoryResponse;
import com.eventmacha.dto.response.PublishResponse;
import com.eventmacha.service.PublishService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Publish flow endpoints: Draft → Preview → Live.
 */
@Path("/publish")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Publish", description = "Event page publish flow: Draft → Preview → Live")
public class PublishController {

    private static final Logger LOG = Logger.getLogger(PublishController.class);

    @Inject
    PublishService publishService;

    /**
     * POST /publish/save-draft
     */
    @POST
    @Path("/save-draft")
    @Operation(summary = "Save Draft",
            description = "Save or update the draft content (siteConfig, content, images) for an order.")
    @APIResponse(responseCode = "200", description = "Draft saved")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    @APIResponse(responseCode = "404", description = "Order not found")
    public Response saveDraft(@Valid SaveDraftRequest request) {
        LOG.debugf("POST /publish/save-draft orderId=%s", request.getOrderId());
        PublishResponse publish = publishService.saveDraft(request);
        return Response.ok(ApiResponse.ok("Draft saved", publish)).build();
    }

    /**
     * POST /publish/preview
     */
    @POST
    @Path("/preview")
    @Operation(summary = "Generate Preview",
            description = "Generates a secure preview token for sharing a draft before publishing.")
    @APIResponse(responseCode = "200", description = "Preview generated with token")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    @APIResponse(responseCode = "404", description = "Publish record not found")
    @APIResponse(responseCode = "422", description = "Invalid state for preview")
    public Response generatePreview(
            @Valid com.eventmacha.dto.request.PreviewRequest request) {
        LOG.debugf("POST /publish/preview orderId=%s", request.getOrderId());
        PublishResponse publish = publishService.generatePreview(request.getOrderId());
        return Response.ok(ApiResponse.ok("Preview generated", publish)).build();
    }

    /**
     * POST /publish/live
     */
    @POST
    @Path("/live")
    @Operation(summary = "Publish Live",
            description = "Promotes draft content to published state and copies to publish history.")
    @APIResponse(responseCode = "200", description = "Published successfully")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    @APIResponse(responseCode = "404", description = "Publish record not found")
    @APIResponse(responseCode = "422", description = "No draft content to publish")
    public Response publishLive(@Valid PublishLiveRequest request) {
        LOG.debugf("POST /publish/live orderId=%s", request.getOrderId());
        PublishResponse publish = publishService.publishLive(request);
        return Response.ok(ApiResponse.ok("Published successfully", publish)).build();
    }

    /**
     * GET /publish/{orderId}
     */
    @GET
    @Path("/{orderId}")
    @Operation(summary = "Get Publish State",
            description = "Returns the current publish state for an order.")
    @APIResponse(responseCode = "200", description = "Publish state")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    @APIResponse(responseCode = "404", description = "Not found")
    public Response getPublish(
            @PathParam("orderId")
            @Parameter(description = "Order ID", required = true) String orderId) {
        LOG.debugf("GET /publish/%s", orderId);
        PublishResponse publish = publishService.getPublish(orderId);
        return Response.ok(ApiResponse.ok(publish)).build();
    }

    /**
     * GET /publish/history/{orderId}
     */
    @GET
    @Path("/history/{orderId}")
    @Operation(summary = "Get Publish History",
            description = "Returns all publish history versions for an order, newest first.")
    @APIResponse(responseCode = "200", description = "Publish history list")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    public Response getPublishHistory(
            @PathParam("orderId")
            @Parameter(description = "Order ID", required = true) String orderId) {
        LOG.debugf("GET /publish/history/%s", orderId);
        List<PublishHistoryResponse> history = publishService.getPublishHistory(orderId);
        return Response.ok(ApiResponse.ok(PaginatedResponse.of(history))).build();
    }
}
