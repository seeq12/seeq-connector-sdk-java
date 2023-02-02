package com.seeq.link.connector.flexgen;

import java.util.UUID;

import com.seeq.link.connector.flexgen.config.MongoDbConnectionConfigV1;
import com.seeq.link.connector.flexgen.config.MongoDbConnectorConfigV1;
import com.seeq.link.sdk.ConfigObject;
import com.seeq.link.sdk.interfaces.ConnectorServiceV2;
import com.seeq.link.sdk.interfaces.ConnectorV2;

/**
 * Implements the {@link ConnectorV2} interface for facilitating dataflow from FlexGen MongoDB systems with Seeq Server.
 */
public class MongoDbConnector implements ConnectorV2 {
    private ConnectorServiceV2 connectorService;
    private MongoDbConnectorConfigV1 connectorConfig;

    @Override
    public String getName() {
        // This name will be used for the configuration file that is found in the data/configuration/link folder.
        return "FlexGen MongoDB Connector";
    }

    @Override
    public void initialize(ConnectorServiceV2 connectorService) throws Exception {
        this.connectorService = connectorService;

        // First, load your configuration using the connector service. If the configuration file is not found, the first
        // object in the passed-in array is returned.
        ConfigObject configObj = this.connectorService.loadConfig(new ConfigObject[] { new MongoDbConnectorConfigV1() });
        this.connectorConfig = (MongoDbConnectorConfigV1) configObj;

        if (this.connectorConfig.getConnections().isEmpty()) {
            MongoDbConnectionConfigV1 defaultConfig = new MongoDbConnectionConfigV1();
            defaultConfig.setName("MongoDB Connection");
            defaultConfig.setId(UUID.randomUUID().toString());
            defaultConfig.setDatabaseName("");
            defaultConfig.setFieldForConditionName("source");
            defaultConfig.setConnectionString("mongodb://localhost:27017");
            defaultConfig.setCollectionName("events");
            defaultConfig.setTimestampField("time");


            this.connectorConfig.getConnections().add(defaultConfig);

            // Save the new (default) configuration file so that the user can see it and modify it themselves
            saveConfig();
        }

        // Now instantiate your connections based on the configuration.
        // Iterate through the configurations to create connection objects.
        for (MongoDbConnectionConfigV1 connectionConfig : this.connectorConfig.getConnections()) {
            if (connectionConfig.getId() == null) {
                // If the ID is null, then the user likely copy/pasted an existing connection configuration and
                // removed the ID so that a new one would be generated. Generate the new one!
                connectionConfig.setId(UUID.randomUUID().toString());
            }

            this.connectorService.addConnection(new MongoDbConnection(this, connectionConfig));
        }

        // Finally, save the connector configuration in a file for the user to view and modify as needed
        saveConfig();
    }

    @Override
    public void destroy() {
        // Perform any connector-wide cleanup as necessary here
    }

    public void saveConfig() {
        // This may be called after indexing activity to save the next scheduled indexing date/time
        this.connectorService.saveConfig(this.connectorConfig);
    }
}
