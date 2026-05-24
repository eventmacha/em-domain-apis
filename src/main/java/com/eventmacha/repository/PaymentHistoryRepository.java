package com.eventmacha.repository;

import com.eventmacha.config.TableNamesConfig;
import com.eventmacha.entity.PaymentHistoryEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;

/**
 * DynamoDB repository for PaymentHistory audit log.
 */
@ApplicationScoped
public class PaymentHistoryRepository {

    private final DynamoDbTable<PaymentHistoryEntity> table;

    @Inject
    public PaymentHistoryRepository(DynamoDbEnhancedClient enhancedClient, TableNamesConfig tableNames) {
        this.table = enhancedClient.table(
                tableNames.paymentHistory(),
                TableSchema.fromBean(PaymentHistoryEntity.class));
    }

    /** Append a new history record (SK = eventTime millis). */
    public void save(PaymentHistoryEntity entity) {
        table.putItem(entity);
    }

    /** Retrieve all history events for a payment sorted by eventTime ascending. */
    public List<PaymentHistoryEntity> findByPaymentId(String paymentId) {
        var request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(paymentId).build()))
                .scanIndexForward(true) // chronological
                .build();
        return table.query(request).stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }
}
