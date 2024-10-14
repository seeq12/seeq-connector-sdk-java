package com.mycompany.seeq.link.connector;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import com.seeq.link.sdk.ConfigObject;
import com.seeq.link.sdk.interfaces.ConnectorServiceV2;
import com.seeq.link.sdk.interfaces.ConnectorV2;

/**
 * Implements the {@link ConnectorV2} interface for facilitating dataflow from external systems with Seeq Server.
 */
public class MyConnector implements ConnectorV2 {
    private ConnectorServiceV2 connectorService;
    private MyConnectorConfigV1 connectorConfig;

    @Override
    public String getName() {
        // This name will be used for the configuration file that is found in the data/configuration/link folder.
        return "MyCompany MyConnector";
    }

    @Override
    public void initialize(ConnectorServiceV2 connectorService) throws Exception {
        this.connectorService = connectorService;

        // First, load your configuration using the connector service. If the configuration file is not found, the first
        // object in the passed-in array is returned.
        ConfigObject configObj = this.connectorService.loadConfig(new ConfigObject[] { new MyConnectorConfigV1() });
        this.connectorConfig = (MyConnectorConfigV1) configObj;

        // Check to see if there are any connections configured yet
        if (this.connectorConfig.getConnections().size() == 0) {
            // Create a default connection configuration
            MyConnectionConfigV1 connectionConfig = new MyConnectionConfigV1();

            // The user will likely change this. It's what will appear in the list of datasources in Seeq Workbench.
            connectionConfig.setName("My First Connection");

            // The identifier must be unique. It need not be a UUID, but that's recommended in lieu of anything else.
            connectionConfig.setId(UUID.randomUUID().toString());

            // Normally you would probably leave the default connection disabled to start, but for this example we
            // want to start up in a functioning state.
            connectionConfig.setEnabled(true);

            // These configuration variables are specific to the MyConnector example. You'll likely remove them.
            // We'll specify a large enough tag count that we observe the batching mechanism in action.
            connectionConfig.setTagCount(5000);
            connectionConfig.setSamplePeriod("15m");

            // Add the new connection configuration to its parent connector
            this.connectorConfig.getConnections().add(connectionConfig);
        }

        // Now instantiate your connections based on the configuration.
        // Iterate through the configurations to create connection objects.
        for (MyConnectionConfigV1 connectionConfig : this.connectorConfig.getConnections()) {
            if (connectionConfig.getId() == null) {
                // If the ID is null, then the user likely copy/pasted an existing connection configuration and
                // removed the ID so that a new one would be generated. Generate the new one!
                connectionConfig.setId(UUID.randomUUID().toString());
            }

            if (!connectionConfig.isEnabled()) {
                // If the connection is not enabled, then do not add it to the list of connections
                continue;
            }

            // do further validation of the connection configuration to ensure only properly configured connections
            // are processed. In our case, we need a valid SamplePeriod, and if a TagCount is provided, it must not be
            // a negative value.
            if (connectionConfig.getSamplePeriod() == null || connectionConfig.getSamplePeriod().isEmpty()) {
                // provide details of the invalid configuration so it can be addressed
                this.connectorService.log().warn("Connection '{}' has an invalid SamplePeriod. It will be ignored.",
                        connectionConfig.getName());

                // you can also disable the connection so it is no longer processed until changes are made
                connectionConfig.setEnabled(false);

                continue;
            }

            if (connectionConfig.getTagCount() < 0) {
                this.connectorService.log().warn("Connection '{}' has an invalid TagCount. It will be ignored.",
                        connectionConfig.getName());
                connectionConfig.setEnabled(false);
                continue;
            }

            this.connectorService.addConnection(new MyConnection(this, connectionConfig));
        }

        // Finally, save the connector configuration in a file for the user to view and modify as needed
        this.connectorService.saveConfig(this.connectorConfig);
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
