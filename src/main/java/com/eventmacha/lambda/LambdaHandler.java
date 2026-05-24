package com.eventmacha.lambda;

/**
 * AWS Lambda handler entry point for the Event Macha API.
 *
 * <p>Quarkus's {@code quarkus-amazon-lambda-http} extension automatically provides
 * {@link io.quarkus.amazon.lambda.http.QuarkusHttpHandler} which bridges API Gateway
 * HTTP API (payload format v2) events to the Quarkus JAX-RS runtime.
 *
 * <p>The Lambda handler is configured via {@code application.properties}:
 * <pre>
 *   quarkus.lambda.handler=em-domain-apis
 * </pre>
 *
 * <p>The actual handler class registered with AWS Lambda is:
 * <pre>
 *   io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest
 * </pre>
 *
 * <p>This class exists as a documentation anchor and allows future customization
 * of the Lambda lifecycle (e.g. pre-warming, custom metrics).
 *
 * @see io.quarkus.amazon.lambda.http.QuarkusHttpHandler
 */
public class LambdaHandler {

    private LambdaHandler() {
        // Not instantiated directly – Quarkus manages the Lambda lifecycle.
    }

    /**
     * Lambda handler reference to specify in AWS Lambda configuration:
     * {@code io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest}
     */
    public static final String HANDLER_CLASS =
            "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest";
}
