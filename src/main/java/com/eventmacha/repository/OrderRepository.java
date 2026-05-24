package com.eventmacha.repository;

import com.eventmacha.config.TableNamesConfig;
import com.eventmacha.entity.OrderEntity;
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
 * DynamoDB repository for Orders.
 */
@ApplicationScoped
public class OrderRepository {

    private static final Logger LOG = Logger.getLogger(OrderRepository.class);

    private final DynamoDbTable<OrderEntity> table;

    @Inject
    public OrderRepository(DynamoDbEnhancedClient enhancedClient, TableNamesConfig tableNames) {
        this.table = enhancedClient.table(tableNames.orders(), TableSchema.fromBean(OrderEntity.class));
    }

    public void save(OrderEntity order) {
        LOG.debugf("Saving order: %s", order.getOrderId());
        table.putItem(order);
    }

    public Optional<OrderEntity> findById(String orderId) {
        return Optional.ofNullable(
                table.getItem(Key.builder().partitionValue(orderId).build()));
    }

    /**
     * Find orders for a user sorted by createdAt descending via user-created-index GSI.
     */
    public List<OrderEntity> findByUserId(String userId) {
        DynamoDbIndex<OrderEntity> index = table.index("user-created-index");
        var request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(userId).build()))
                .scanIndexForward(false) // newest first
                .build();
        return index.query(request).stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }
}
