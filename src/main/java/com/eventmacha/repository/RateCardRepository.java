package com.eventmacha.repository;

import com.eventmacha.config.TableNamesConfig;
import com.eventmacha.entity.RateCardEntity;
import com.eventmacha.entity.RateCardPlanEntity;
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
 * DynamoDB repository for RateCards and RateCardPlans.
 */
@ApplicationScoped
public class RateCardRepository {

    private static final Logger LOG = Logger.getLogger(RateCardRepository.class);

    private final DynamoDbTable<RateCardEntity> rateCardTable;
    private final DynamoDbTable<RateCardPlanEntity> planTable;

    @Inject
    public RateCardRepository(DynamoDbEnhancedClient enhancedClient, TableNamesConfig tableNames) {
        this.rateCardTable = enhancedClient.table(tableNames.rateCards(), TableSchema.fromBean(RateCardEntity.class));
        this.planTable = enhancedClient.table(tableNames.rateCardPlans(), TableSchema.fromBean(RateCardPlanEntity.class));
    }

    // ── RateCard ──────────────────────────────────────────────────────────────

    public void saveRateCard(RateCardEntity entity) {
        rateCardTable.putItem(entity);
    }

    public Optional<RateCardEntity> findRateCardById(String rateCardId) {
        return Optional.ofNullable(
                rateCardTable.getItem(Key.builder().partitionValue(rateCardId).build()));
    }

    /** Query RateCards by userType via the userType-index GSI. */
    public List<RateCardEntity> findRateCardsByUserType(String userType) {
        DynamoDbIndex<RateCardEntity> index = rateCardTable.index("userType-index");
        var request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(userType).build()))
                .build();
        return index.query(request).stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }

    // ── RateCardPlan ──────────────────────────────────────────────────────────

    public void savePlan(RateCardPlanEntity entity) {
        planTable.putItem(entity);
    }

    public Optional<RateCardPlanEntity> findPlan(String rateCardId, String planType) {
        return Optional.ofNullable(
                planTable.getItem(Key.builder()
                        .partitionValue(rateCardId)
                        .sortValue(planType)
                        .build()));
    }

    /** Query all plans for a given rateCardId. */
    public List<RateCardPlanEntity> findPlansByRateCardId(String rateCardId) {
        var request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(rateCardId).build()))
                .build();
        return planTable.query(request).stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }
}
