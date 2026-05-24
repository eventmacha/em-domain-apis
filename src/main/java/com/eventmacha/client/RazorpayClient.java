package com.eventmacha.client;

import com.eventmacha.config.AppConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

/**
 * HTTP client for the Razorpay REST API.
 *
 * <p>Uses {@link java.net.http.HttpClient} (JDK built-in) to avoid additional
 * dependencies – important for Lambda cold-start optimisation.
 *
 * <p>API Reference: https://razorpay.com/docs/api/
 */
@ApplicationScoped
public class RazorpayClient {

    private static final Logger LOG = Logger.getLogger(RazorpayClient.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String basicAuthHeader;
    private final String webhookSecret;

    @Inject
    public RazorpayClient(AppConfig appConfig) {
        this.baseUrl = appConfig.razorpay().baseUrl();
        this.webhookSecret = appConfig.razorpay().webhookSecret();

        String credentials = appConfig.razorpay().keyId() + ":" + appConfig.razorpay().keySecret();
        this.basicAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // ── Order ─────────────────────────────────────────────────────────────────

    /**
     * Create a Razorpay order.
     *
     * @param amountPaise amount in paise (INR smallest unit), e.g. 50000 = ₹500
     * @param currency    currency code, e.g. "INR"
     * @param receiptId   unique receipt ID (your orderId)
     * @return Razorpay response as JsonNode
     */
    public JsonNode createOrder(long amountPaise, String currency, String receiptId) {
        Map<String, Object> body = Map.of(
                "amount", amountPaise,
                "currency", currency,
                "receipt", receiptId
        );
        return post("/orders", body);
    }

    // ── Webhook Signature ─────────────────────────────────────────────────────

    /**
     * Verify Razorpay webhook signature.
     *
     * <p>Razorpay sends the header {@code X-Razorpay-Signature} which is
     * HMAC-SHA256 of the raw request body signed with the webhook secret.
     *
     * @param rawBody   raw request body bytes
     * @param signature header value of X-Razorpay-Signature
     * @return {@code true} if the signature is valid
     */
    public boolean verifyWebhookSignature(String rawBody, String signature) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);
            boolean valid = computed.equals(signature);
            if (!valid) {
                LOG.warnf("Webhook signature mismatch: expected=%s received=%s", computed, signature);
            }
            return valid;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to verify Razorpay webhook signature");
            return false;
        }
    }

    // ── HTTP helpers ──────────────────────────────────────────────────────────

    private JsonNode post(String path, Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Authorization", basicAuthHeader)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOG.debugf("Razorpay %s %s → %d", "POST", path, response.statusCode());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOG.errorf("Razorpay error [%d]: %s", response.statusCode(), response.body());
                throw new com.eventmacha.exception.BusinessException(
                        "RAZORPAY_ERROR",
                        "Razorpay API error: " + response.statusCode());
            }

            return objectMapper.readTree(response.body());
        } catch (com.eventmacha.exception.BusinessException e) {
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Razorpay HTTP call failed: %s", path);
            throw new com.eventmacha.exception.ApiException(502, "GATEWAY_ERROR",
                    "Payment gateway unavailable");
        }
    }
}
