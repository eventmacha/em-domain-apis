package com.eventmacha.repository;

import com.eventmacha.config.TableNamesConfig;
import com.eventmacha.entity.UserEntity;
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

import java.util.Optional;

/**
 * DynamoDB repository for User operations.
 */
@ApplicationScoped
public class UserRepository {

    private static final Logger LOG = Logger.getLogger(UserRepository.class);

    private final DynamoDbTable<UserEntity> table;

    @Inject
    public UserRepository(DynamoDbEnhancedClient enhancedClient, TableNamesConfig tableNames) {
        this.table = enhancedClient.table(tableNames.users(), TableSchema.fromBean(UserEntity.class));
    }

    /** Persist or replace a user item. */
    public void save(UserEntity user) {
        LOG.debugf("Saving user: %s", user.getUserId());
        table.putItem(user);
    }

    /** Find a user by primary key. */
    public Optional<UserEntity> findById(String userId) {
        UserEntity result = table.getItem(Key.builder().partitionValue(userId).build());
        return Optional.ofNullable(result);
    }

    /** Find a user by email via the email-index GSI. */
    public Optional<UserEntity> findByEmail(String email) {
        DynamoDbIndex<UserEntity> index = table.index("email-index");
        var request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(email).build()))
                .limit(1)
                .build();
        return index.query(request).stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }

    /**
     * Find a user by authProvider + providerUserId via the provider-index GSI.
     * Used to prevent duplicate registrations for the same social identity.
     */
    public Optional<UserEntity> findByProvider(String authProvider, String providerUserId) {
        DynamoDbIndex<UserEntity> index = table.index("provider-index");
        var request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder()
                                .partitionValue(authProvider)
                                .sortValue(providerUserId)
                                .build()))
                .limit(1)
                .build();
        return index.query(request).stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }
}
