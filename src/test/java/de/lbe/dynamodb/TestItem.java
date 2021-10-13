package de.lbe.dynamodb;

import java.util.Objects;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 *
 */
@DynamoDbBean
public class TestItem {

    private String id;

    private String anyValue;

    @DynamoDbPartitionKey
    public String getId() {
        return this.id;
    }

    public void setId(String imei) {
        this.id = Objects.requireNonNull(imei);
    }

    @Override
    public String toString() {
        return String.format("UpdateItem[%s]", id);
    }

    public String getAnyValue() {
        return anyValue;
    }

    public void setAnyValue(String attr) {
        this.anyValue = attr;
    }
}
