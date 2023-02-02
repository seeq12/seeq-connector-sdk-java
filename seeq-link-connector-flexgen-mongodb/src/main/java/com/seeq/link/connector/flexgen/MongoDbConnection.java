package com.seeq.link.connector.flexgen;

import com.google.common.annotations.VisibleForTesting;
import com.mongodb.client.*;
import com.seeq.link.connector.flexgen.config.MongoDbConnectionConfigV1;
import com.seeq.link.sdk.DefaultIndexingDatasourceConnectionConfig;
import com.seeq.link.sdk.interfaces.*;
import com.seeq.link.sdk.interfaces.Connection.ConnectionState;
import com.seeq.link.sdk.utilities.Capsule;
import com.seeq.link.sdk.utilities.TimeInstant;
import com.seeq.model.*;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;

/**
 * Represents a connection to a unique datasource. A connector can host any number of such connections to
 * datasources.
 *
 * This example implements the {@link SignalPullDatasourceConnection} interface, which means that
 * the connection responds to on-demand requests from Seeq Server for Samples within a Signal and queries
 * its datasource to produce the result.
 *
 * A connection can also implement {@link com.seeq.link.sdk.interfaces.ConditionPullDatasourceConnection}
 * to respond to on-demand requests from Seeq Server for Capsules within a Condition.
 *
 * Alternatively, a connection could choose to implement neither of the above interfaces and instead "push"
 * Samples or Capsules into Seeq using the {@link com.seeq.api.SignalsApi} or {@link com.seeq.api.ConditionsApi}
 * obtained via {@link com.seeq.link.sdk.interfaces.SeeqApiProvider} on
 * {@link com.seeq.link.sdk.interfaces.AgentService} on {@link DatasourceConnectionServiceV2}.
 */
public class MongoDbConnection implements ConditionPullDatasourceConnection {
    private final String ID_DELIMITER = ":";
    private final MongoDbConnector connector;
    private final MongoDbConnectionConfigV1 connectionConfig;
    private final String rootAssetId;
    private DatasourceConnectionServiceV2 connectionService;
    private final Bson pingCommand = new BsonDocument("ping", new BsonInt64(1));
    private MongoClient client;
    private MongoDatabase mongoDatabase;
    private final long conditionDuration;

    public MongoDbConnection(MongoDbConnector connector, MongoDbConnectionConfigV1 connectionConfig) {
        this.connector = connector;
        this.connectionConfig = connectionConfig;
        this.conditionDuration = this.connectionConfig.getConditionDuration() * 60000000000L;
        this.rootAssetId = getDatasourceClass() + ID_DELIMITER + this.connectionConfig.getDatabaseName();
    }

    @Override
    // Return a string that identifies this type of datasource. Example: "ERP System"
    // This value will be seen in the Information panel in Seeq Workbench.
    public String getDatasourceClass() {
        return "MongoDB";
    }

    @Override
    public String getDatasourceName() {
        // The name will appear in Seeq Workbench and can change (as long as the DatasourceId does not change)
        return this.connectionConfig.getName();
    }

    @Override
    public String getDatasourceId() {
        // This unique identifier usually must come from the configuration file and be unchanging
        return this.connectionConfig.getId();
    }

    @Override
    // The configuration should extend DefaultIndexingDatasourceConnectionConfig so that concerns like property
    // transforms and index scheduling are taken care of by the SDK.
    public DefaultIndexingDatasourceConnectionConfig getConfiguration() {
        return this.connectionConfig;
    }

    // You probably won't do much in the initialize() function. But if you have to do some I/O that is separate
    // from the act of connecting, you could do it here.
    @Override
    public void initialize(DatasourceConnectionServiceV2 connectionService) {
        this.connectionService = connectionService;
        // Validate the connection configuration
        String errorMsg = "";
        if (this.connectionConfig.getFieldForConditionName() == null ||
                this.connectionConfig.getFieldForConditionName().isEmpty()){
            errorMsg = errorMsg + "fieldForConditionName is null or empty. Please fix the configuration.";
        }
        if (this.connectionConfig.getTimestampField() == null ||
                this.connectionConfig.getTimestampField().isEmpty()){
            errorMsg += "timestampField is null or empty. Please fix the configuration.";
        }
        if (this.connectionConfig.getDatabaseName() == null ||
                this.connectionConfig.getDatabaseName().isEmpty()){
            errorMsg += "databaseName is null or empty. Please fix the configuration.";
        }
        if (this.connectionConfig.getConnectionString() == null ||
                this.connectionConfig.getConnectionString().isEmpty()){
            errorMsg += "connectionString is null or empty. Please fix the configuration.";
        }
        if (!errorMsg.isEmpty()){
            connectionService.log().error(errorMsg);
            return;
        }
        // It's your job to inspect your configuration to see if the user has enabled this connection.
        if (this.connectionConfig.isEnabled()) {
            // This will cause the connect/monitor thread to be spawned and connect() to be called
            this.connectionService.enable();
        }
    }

    @Override
    public void connect() {
        this.connectionService.setConnectionState(ConnectionState.CONNECTING);

        this.client = MongoClients.create(connectionConfig.getConnectionString());
        this.mongoDatabase = this.client.getDatabase(this.connectionConfig.getDatabaseName());
        if (testConnection()){
            this.connectionService.setConnectionState(ConnectionState.CONNECTED);
            return;
        }
        this.connectionService.setConnectionState(ConnectionState.DISCONNECTED);
    }

    @Override
    public boolean monitor() {
        try {
            return testConnection();
        } catch (Exception ex){
            return false;
        }
    }

    //This method executes a BSON Ping command against the configured database. This no-op command is the preferred
    //mechanism over the ServerMonitorListener due to the need for the listener to register callbacks. There is a chance
    //of a race condition with the listener vs the monitor thread running, therefore, we opted for the sync no-op ping.
    boolean testConnection(){
        try {
            mongoDatabase.runCommand(pingCommand);
            return true;
        } catch (Exception ex){
            this.connectionService.setConnectionState(ConnectionState.DISCONNECTED);
            this.connectionService.log().error("Error connecting to cluster. Please check the connector configuration " +
                    "or the network link.");
            return false;
        }
    }

    @Override
    public void disconnect() {
        // Transition to the disconnected state.
        this.connectionService.setConnectionState(ConnectionState.DISCONNECTED);
        client.close();
    }

    @Override
    public void index(SyncMode syncMode) {
        createRootAsset();
        indexConditions();
    }

    @VisibleForTesting
    // This method uses the field specified in the configuration property (FieldForConditionName) to identify the
    // distinct sources that a capsule could be generated in. In the sample data set there was a column named source
    // that identified one of 4 source that a capsule could originate from. Each source is then a condition, that contains
    // the individual events as capsules
    void indexConditions() {
        MongoCollection<Document> collection = mongoDatabase.getCollection(this.connectionConfig.getCollectionName());
        DistinctIterable<String> sources = collection.distinct(this.connectionConfig.getFieldForConditionName(), String.class);
        sources.forEach((Consumer<String>) source -> {
                this.connectionService.putCondition(getConditionForSource(source));
                this.connectionService.putRelationship(createRelationship(source));
            }
        );
    }

    private ConditionUpdateInputV1 getConditionForSource(String source){
        ConditionUpdateInputV1 sourceCondition = new ConditionUpdateInputV1();
        sourceCondition.setName(source);
        sourceCondition.setDataId(this.connectionConfig.getDatabaseName() + ID_DELIMITER + source);
        sourceCondition.maximumDuration(this.connectionConfig.getConditionDuration() + " minutes");
        CapsulePropertyInputV1 severity = new CapsulePropertyInputV1();
        severity.setName("Severity");
        severity.setUnitOfMeasure("");
        CapsulePropertyInputV1 message = new CapsulePropertyInputV1();
        message.setName("Message");
        message.setUnitOfMeasure("string");
        sourceCondition.addCapsulePropertiesItem(severity);
        sourceCondition.addCapsulePropertiesItem(message);
        return sourceCondition;
    }

    private AssetTreeSingleInputV1 createRelationship(String source){
        AssetTreeSingleInputV1 relationship = new AssetTreeSingleInputV1();
        relationship.setParentDataId(rootAssetId);
        relationship.setChildDataId(this.connectionConfig.getDatabaseName() + ID_DELIMITER + source);
        return relationship;
    }

    private void createRootAsset() {
        AssetInputV1 rootAssetInput = new AssetInputV1();
        rootAssetInput.setHostId(this.connectionService.getDatasource().getId());
        rootAssetInput.setDataId(rootAssetId);
        rootAssetInput.setName(this.connectionConfig.getDatabaseName());

        this.connectionService.putRootAsset(rootAssetInput);
        this.connectionService.flushRootAssets();
    }

    // This parameter can help control the load that Seeq puts on an external datasource. It is typically
    // controlled from the configuration file.
    @Override
    public Integer getMaxConcurrentRequests() {
        return this.connectionConfig.getMaxConcurrentRequests();
    }

    // This parameter can help control the load and memory usage that Seeq puts on an external datasource. It is
    // typically controlled from the configuration file.
    @Override
    public Integer getMaxResultsPerRequest() {
        return this.connectionConfig.getMaxResultsPerRequest();
    }

    @Override
    public void saveConfig() {
        this.connector.saveConfig();
    }

    @VisibleForTesting
    String getParentAsset(String dataId){
        return dataId.split(ID_DELIMITER)[1];
    }

    @Override
    public Stream<Capsule> getCapsules(GetCapsulesParameters parameters) throws Exception {
        long startTime = parameters.getOverlappingStartTime().roundDown(1000).getTimestamp()/1000 - 1;
        long endTime = parameters.getExpandedEndTime().roundUp(1000).getTimestamp()/1000 + 1;
        String parent = getParentAsset(parameters.getDataId());
        var result = getCapsulesInternal(endTime, startTime, parent);
        List<Capsule> foundCapsules = new ArrayList<>();
        result.forEach((Consumer<Document>) doc -> {
            long capsuleTime = (long)doc.get(connectionConfig.getTimestampField())*1000;
            TimeInstant capsuleStart = new TimeInstant(capsuleTime);
            TimeInstant capsuleEnd = new TimeInstant(capsuleTime+this.conditionDuration);
            List<Capsule.Property> properties = new ArrayList<>();
            properties.add(new Capsule.Property("Severity", doc.get("severity").toString(), ""));
            properties.add(new Capsule.Property("Message", doc.get("message").toString(), "string"));
            Capsule capsule = new Capsule(capsuleStart, capsuleEnd, properties);
            foundCapsules.add(capsule);
        });
        return foundCapsules.stream();
    }

    @VisibleForTesting
    FindIterable<Document> getCapsulesInternal(long endTime, long startTime, String parent){
        Bson capsuleIdentification = and(eq("source", parent), and(gte("time", startTime), lte("time", endTime)));
        return mongoDatabase.getCollection("events").find(capsuleIdentification).sort(ascending("time"));
    }
}
