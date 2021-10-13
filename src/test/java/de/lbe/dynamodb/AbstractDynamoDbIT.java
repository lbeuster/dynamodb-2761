package de.lbe.dynamodb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_SECONDS;
import static org.awaitility.Durations.ONE_SECOND;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

/**
 * @author lbeuster
 */
@Testcontainers
abstract class AbstractDynamoDbIT {

    /**
     * If true all tests go the a locally started localstack instance. If false all tests go to AWS.
     */
    private static final boolean RUN_ON_LOCALSTACK = false;

    /**
     * If false we have to create the table with cloudformation.
     */
    private static final boolean AUTO_CREATE_TABLE_ON_AWS = false;

    public static final String TABLE = "UpdateItem";

    private static LocalStackContainer localstack = null;

    private static DynamoDbClient client = null;

    private static DynamoDbEnhancedClient enhancedClient = null;

    private static DynamoDbTable<TestItem> table = null;

    private TestInfo testInfo;

    /**
     *
     */
    @SuppressWarnings({ "resource", "unused" })
    @BeforeAll
    static void setUpLocalstack() {

        // create infrastructure + clients
        boolean createTable = AUTO_CREATE_TABLE_ON_AWS;
        if (RUN_ON_LOCALSTACK && localstack == null) {
            localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.18")).withServices(Service.DYNAMODB);
            localstack.start();
            client = newLocalstackDynamoDbClient(localstack);
            createTable = true;
        } else {
            client = newAwsDynamoDbClient();
        }
        enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
        table = enhancedClient.table(TABLE, TableSchema.fromBean(TestItem.class));

        // create table
        if (createTable) {
            createTable();
        }
    }

    private static void createTable() {
        SimpleDynamoDbTableClient tableClient = new SimpleDynamoDbTableClient(client);

        // delete
        try {
            table.deleteTable();
        } catch (@SuppressWarnings("unused") ResourceNotFoundException ex) {
            // ignore
        }

        // needs a little bit time on AWS to really delete it
        await().atMost(FIVE_SECONDS).pollInterval(ONE_SECOND).untilAsserted(() -> {
            assertThat(tableClient.existsTable(TABLE)).isFalse();
        });

        // create
        table.createTable();

        // it's not enough on AWS to check if the table exists - we have to check that we can invoke item operations
        // 10 secs is not enough
        await().atMost(Duration.ofSeconds(30)).pollInterval(ONE_SECOND).untilAsserted(() -> {
            try {
                String id = "test_delete";
                Key key = Key.builder().partitionValue(id).build();
                table.deleteItem(key);
            } catch (@SuppressWarnings("unused") ResourceNotFoundException ex) {
                fail();
            }
        });
    }

    @BeforeEach
    void initTestInfo(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    private static DynamoDbClient newAwsDynamoDbClient() {
        return DynamoDbClient.create();
    }

    private static DynamoDbClient newLocalstackDynamoDbClient(LocalStackContainer container) {
        Objects.requireNonNull(container);
        // @formatter:off
        return DynamoDbClient.builder()
            .endpointOverride(container.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(container.getAccessKey(), container.getSecretKey())))
            .region(Region.of(container.getRegion()))
            .build();
        // @formatter:on
    }

    protected DynamoDbClient client() {
        return client;
    }

    protected DynamoDbEnhancedClient enhancedClient() {
        return enhancedClient;
    }

    protected DynamoDbTable<TestItem> table() {
        return table;
    }

    protected String getTestMethodName() {
        return testInfo.getTestMethod().get().getName(); // NOSONAR: Optional.get()
    }
}