package com.eventmacha.repository;

import com.eventmacha.config.TableNamesConfig;
import com.eventmacha.entity.PaymentEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.Optional;

/**
 * DynamoDB repository for Payments.
 */
@ApplicationScoped
public class PaymentRepository {

    private static final Logger LOG = Logger.getLogger(PaymentRepository.class);

    private final DynamoDbTable<PaymentEntity> table;

    @Inject
    public PaymentRepository(DynamoDbEnhancedClient enhancedClient, TableNamesConfig tableNames) {
        this.table = enhancedClient.table(tableNames.payments(), TableSchema.fromBean(PaymentEntity.class));
    }

    public void save(PaymentEntity payment) {
        LOG.debugf("Saving payment: %s", payment.getPaymentId());
        table.putItem(payment);
    }

    public Optional<PaymentEntity> findById(String paymentId) {
        return Optional.ofNullable(
                table.getItem(Key.builder().partitionValue(paymentId).build()));
    }

    /** Find payments by orderId via the order-index GSI. */
    public List<PaymentEntity> findByOrderId(String orderId) {
        DynamoDbIndex<PaymentEntity> index = table.index("order-index");
        var request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(orderId).build()))
                .build();
        return index.query(request).stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }

    /**
     * Find payments for a user sorted by createdAt desc via user-payment-index GSI.
     */
    public List<PaymentEntity> findByUserId(String userId) {
        DynamoDbIndex<PaymentEntity> index = table.index("user-payment-index");
        var request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(userId).build()))
                .scanIndexForward(false)
                .build();
        return index.query(request).stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }
}
