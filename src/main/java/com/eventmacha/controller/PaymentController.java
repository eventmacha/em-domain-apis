package com.eventmacha.controller;

import com.eventmacha.common.ApiResponse;
import com.eventmacha.dto.request.CreatePaymentRequest;
import com.eventmacha.dto.response.PaymentResponse;
import com.eventmacha.service.PaymentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * Payment initiation endpoint.
 */
@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Payments", description = "Initiate Razorpay payments")
public class PaymentController {

    private static final Logger LOG = Logger.getLogger(PaymentController.class);

    @Inject
    PaymentService paymentService;

    /**
     * POST /payments
     * Creates a Razorpay order and records the payment intent.
     * Returns the razorpayOrderId to be used with the client-side Razorpay checkout SDK.
     */
    @POST
    @Operation(summary = "Initiate Payment",
            description = "Creates a Razorpay order for the given orderId. " +
                    "Returns razorpayOrderId for use with the Razorpay checkout SDK.")
    @APIResponse(responseCode = "201", description = "Payment initiated")
    @APIResponse(responseCode = "400", description = "Validation error")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    @APIResponse(responseCode = "422", description = "Order not in PENDING state")
    @APIResponse(responseCode = "502", description = "Razorpay API unavailable")
    public Response createPayment(@Valid CreatePaymentRequest request) {
        LOG.debugf("POST /payments orderId=%s", request.getOrderId());
        PaymentResponse payment = paymentService.createPayment(request);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created(payment))
                .build();
    }
}
