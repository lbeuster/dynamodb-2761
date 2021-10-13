package de.lbe.dynamodb;

import java.util.Objects;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

/**
 * @author lbeuster
 */
public class SimpleDynamoDbTableClient {

    private final DynamoDbClient client;

    public SimpleDynamoDbTableClient(DynamoDbClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public void createTable(String tableName, String partitionKey, ScalarAttributeType partitionKeyType) {
        KeySchemaElement partitionKeySchema = KeySchemaElement.builder().attributeName(partitionKey).keyType(KeyType.HASH).build();
        AttributeDefinition prtitionKeyDef = AttributeDefinition.builder().attributeName(partitionKey).attributeType(partitionKeyType).build();
        // @formatter:off
        client.createTable(b -> b
            .tableName(tableName)
            .keySchema(partitionKeySchema)
            .attributeDefinitions(prtitionKeyDef)
            .provisionedThroughput(t -> t.readCapacityUnits(10L).writeCapacityUnits(10L)));
        // @formatter:on
    }

    public void deleteTable(String tableName) {
        Objects.requireNonNull(tableName);
        client.deleteTable(b -> b.tableName(tableName));
    }

    public boolean existsTable(String tableName) {
        try {
            client.describeTable(b -> b.tableName(tableName));
            return true;
        } catch (@SuppressWarnings("unused") ResourceNotFoundException ex) {
            return false;
        }
    }

    public void deleteTableIfExists(String tableName) {
        if (existsTable(tableName)) {
            deleteTable(tableName);
        }
    }
}
