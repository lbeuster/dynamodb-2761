package de.lbe.dynamodb;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.enhanced.dynamodb.Key;

/**
 * @author lbeuster
 */
class UpdateItemTest extends AbstractDynamoDbIT {

    @Test
    void success_if_ignoreNulls_is_true_and_any_attribute_is_null() {
        test_create_new_item_with_UpdateItem(true, null);
    }

    @Test
    void failure_if_ignoreNulls_is_false_and_any_attribute_is_null() {
        test_create_new_item_with_UpdateItem(false, null);
    }

    @Test
    void success_if_ignoreNulls_is_false_and_any_attribute_is_not_null() {
        test_create_new_item_with_UpdateItem(false, "hello");
    }

    private void test_create_new_item_with_UpdateItem(boolean ignoreNulls, String anyValue) {

        // delete
        String id = getTestMethodName();
        Key key = Key.builder().partitionValue(id).build();
        table().deleteItem(key);

        // read -> no item
        TestItem foundItem = table().getItem(b -> b.key(key).consistentRead(true));
        assertThat(foundItem).isNull();

        // update -> we expect a new item
        TestItem item = new TestItem();
        item.setId(id);
        if (anyValue != null) {
            item.setAnyValue(anyValue);
        }
        TestItem updatedItem = table().updateItem(b -> b.item(item).ignoreNulls(ignoreNulls));

        // read -> we expect the created item
        foundItem = table().getItem(b -> b.key(key).consistentRead(true));
        assertThat(foundItem).withFailMessage("Item could not be found with GetItem after UpdateItem").isNotNull();
        assertThat(updatedItem).isNotNull();
    }
}
