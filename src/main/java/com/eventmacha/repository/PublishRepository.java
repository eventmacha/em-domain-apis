package com.eventmacha.repository;

import com.eventmacha.config.TableNamesConfig;
import com.eventmacha.entity.PublishEntity;
import com.eventmacha.entity.PublishHistoryEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.Optional;

/**
 * DynamoDB repository for Publish and PublishHistory tables.
 */
@ApplicationScoped
public class PublishRepository {

    private static final Logger LOG = Logger.getLogger(PublishRepository.class);

    private final DynamoDbTable<PublishEntity> publishTable;
    private final DynamoDbTable<PublishHistoryEntity> historyTable;

    @Inject
    public PublishRepository(DynamoDbEnhancedClient enhancedClient, TableNamesConfig tableNames) {
        this.publishTable = enhancedClient.table(tableNames.publish(), TableSchema.fromBean(PublishEntity.class));
        this.historyTable = enhancedClient.table(tableNames.publishHistory(), TableSchema.fromBean(PublishHistoryEntity.class));
    }

    // ── Publish ───────────────────────────────────────────────────────────────

    public void save(PublishEntity entity) {
        LOG.debugf("Saving publish record for order: %s", entity.getOrderId());
        publishTable.putItem(entity);
    }

    public Optional<PublishEntity> findByOrderId(String orderId) {
        return Optional.ofNullable(
                publishTable.getItem(Key.builder().partitionValue(orderId).build()));
    }

    // ── PublishHistory ────────────────────────────────────────────────────────

    public void saveHistory(PublishHistoryEntity entity) {
        historyTable.putItem(entity);
    }

    /** Retrieve all publish history versions for an order, newest first. */
    public List<PublishHistoryEntity> findHistoryByOrderId(String orderId) {
        var request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(orderId).build()))
                .scanIndexForward(false) // newest version first
                .build();
        return historyTable.query(request).stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }
}
