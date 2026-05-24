package com.eventmacha.controller;

import com.eventmacha.common.ApiResponse;
import com.eventmacha.dto.request.CreateOrderRequest;
import com.eventmacha.dto.response.OrderResponse;
import com.eventmacha.service.OrderService;
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

/**
 * Order management endpoints.
 */
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Orders", description = "Create and retrieve orders")
public class OrderController {

    private static final Logger LOG = Logger.getLogger(OrderController.class);

    @Inject
    OrderService orderService;

    /**
     * POST /orders
     */
    @POST
    @Operation(summary = "Create Order",
            description = "Creates a new order for the authenticated user based on a rate card and plan.")
    @APIResponse(responseCode = "201", description = "Order created")
    @APIResponse(responseCode = "400", description = "Validation error")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    @APIResponse(responseCode = "404", description = "Rate card or plan not found")
    public Response createOrder(@Valid CreateOrderRequest request) {
        LOG.debugf("POST /orders rateCardId=%s planType=%s", request.getRateCardId(), request.getPlanType());
        OrderResponse order = orderService.createOrder(request);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created(order))
                .build();
    }

    /**
     * GET /orders/{id}
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Get Order",
            description = "Retrieves an order by ID. Users can only access their own orders.")
    @APIResponse(responseCode = "200", description = "Order found")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    @APIResponse(responseCode = "404", description = "Order not found")
    public Response getOrder(
            @PathParam("id")
            @Parameter(description = "Order ID", required = true) String orderId) {
        LOG.debugf("GET /orders/%s", orderId);
        OrderResponse order = orderService.getOrder(orderId);
        return Response.ok(ApiResponse.ok(order)).build();
    }
}
