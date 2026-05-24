package com.eventmacha.controller;

import com.eventmacha.common.ApiResponse;
import com.eventmacha.service.PaymentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * Razorpay webhook receiver.
 *
 * <p>This endpoint is intentionally open (no JWT required) because Razorpay
 * cannot send a Cognito JWT. Authentication is performed by verifying the
 * {@code X-Razorpay-Signature} HMAC-SHA256 header.
 */
@Path("/webhooks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Webhooks", description = "Razorpay webhook receiver")
public class WebhookController {

    private static final Logger LOG = Logger.getLogger(WebhookController.class);

    @Inject
    PaymentService paymentService;

    /**
     * POST /webhooks/razorpay
     *
     * <p>Razorpay POSTs all payment events here. The raw body is used for
     * HMAC-SHA256 signature verification before processing.
     */
    @POST
    @Path("/razorpay")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Razorpay Webhook",
            description = "Receives Razorpay payment events (payment.captured, payment.failed, etc.). " +
                    "Verifies the X-Razorpay-Signature header before processing.")
    @APIResponse(responseCode = "200", description = "Webhook processed")
    @APIResponse(responseCode = "400", description = "Invalid signature or payload")
    public Response handleRazorpayWebhook(
            String rawBody,
            @HeaderParam("X-Razorpay-Signature")
            @Parameter(description = "Razorpay HMAC-SHA256 signature", required = true)
            String signature) {

        LOG.infof("Received Razorpay webhook, signature present: %b", signature != null);

        if (signature == null || signature.isBlank()) {
            return Response.status(400)
                    .entity(new com.eventmacha.exception.ApiErrorResponse(
                            "MISSING_SIGNATURE", "X-Razorpay-Signature header is required"))
                    .build();
        }

        paymentService.handleWebhook(rawBody, signature);
        return Response.ok(ApiResponse.ok("Webhook processed")).build();
    }
}
