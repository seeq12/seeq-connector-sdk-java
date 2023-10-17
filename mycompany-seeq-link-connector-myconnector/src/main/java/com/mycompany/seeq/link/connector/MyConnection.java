package com.mycompany.seeq.link.connector;

import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.google.common.math.LongMath;
import com.mycompany.seeq.link.connector.DatasourceSimulator.Waveform;
import com.seeq.link.sdk.DefaultIndexingDatasourceConnectionConfig;
import com.seeq.link.sdk.interfaces.ConditionPullDatasourceConnection;
import com.seeq.link.sdk.interfaces.Connection.ConnectionState;
import com.seeq.link.sdk.interfaces.DatasourceConnectionServiceV2;
import com.seeq.link.sdk.interfaces.GetCapsulesParameters;
import com.seeq.link.sdk.interfaces.GetSamplesParameters;
import com.seeq.link.sdk.interfaces.SignalPullDatasourceConnection;
import com.seeq.link.sdk.interfaces.SyncMode;
import com.seeq.link.sdk.utilities.Capsule;
import com.seeq.link.sdk.utilities.Sample;
import com.seeq.link.sdk.utilities.TimeInstant;
import com.seeq.model.AssetInputV1;
import com.seeq.model.AssetTreeSingleInputV1;
import com.seeq.model.ConditionInputV1;
import com.seeq.model.ConditionUpdateInputV1;
import com.seeq.model.SignalWithIdInputV1;

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
public class MyConnection implements SignalPullDatasourceConnection, ConditionPullDatasourceConnection {
    private final MyConnector connector;
    private final MyConnectionConfigV1 connectionConfig;
    private DatasourceConnectionServiceV2 connectionService;
    private DatasourceSimulator datasourceSimulator;
    private Duration samplePeriod;

    public MyConnection(MyConnector connector, MyConnectionConfigV1 connectionConfig) {
        // You will generally want to accept a configuration object from your connector parent. Do not do any I/O in the
        // constructor -- leave that for the other functions like initialize() or connect(). Generally, you should just
        // be setting private fields in the constructor.
        this.connector = connector;
        this.connectionConfig = connectionConfig;
        this.datasourceSimulator = null;
    }

    @Override
    public String getDatasourceClass() {
        // Return a string that identifies this type of datasource. Example: "ERP System"
        // This value will be seen in the Information panel in Seeq Workbench.
        return "My Connector Type";
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
    public DefaultIndexingDatasourceConnectionConfig getConfiguration() {
        // The configuration should extend DefaultIndexingDatasourceConnectionConfig so that concerns like property
        // transforms and index scheduling are taken care of by the SDK.
        return this.connectionConfig;
    }

    @Override
    public void initialize(DatasourceConnectionServiceV2 connectionService) {
        // You probably won't do much in the initialize() function. But if you have to do some I/O that is separate
        // from the act of connecting, you could do it here.

        this.connectionService = connectionService;

        // It's your job to inspect your configuration to see if the user has enabled this connection.
        if (this.connectionConfig.isEnabled()) {
            // This will cause the connect/monitor thread to be spawned and connect() to be called
            this.connectionService.enable();
        }
    }

    @Override
    public void connect() {
        // First, notify the connection service that you're attempting to connect. You must go through this CONNECTING
        // state before you go to CONNECTED, otherwise the CONNECTED state will be ignored.
        this.connectionService.setConnectionState(ConnectionState.CONNECTING);

        // These lines are specific to the simulator example.
        this.samplePeriod = Duration.parse("PT" + this.connectionConfig.getSamplePeriod().toUpperCase());
        Duration signalPeriod = this.samplePeriod.multipliedBy(100);

        // Use logging statements to show important information in the log files. These logging statements will be
        // output to the console when you're in the IDE and also to "java/seeq-link-sdk-debugging-agent/target/log/
        // jvm-debugging-agent.log" within the Connector SDK. When you have deployed your connector, the log statements
        // will go to the "log/jvm-link/jvm-link.log" file in the Seeq data folder.
        this.connectionService.log().debug("Sample period parsed as '{}'", this.samplePeriod);
        this.connectionService.log().debug("Signal period determined to be '{}'", signalPeriod);

        // Second, perform whatever I/O is necessary to establish a connection to your datasource. For example, you
        // might instantiate a JDBC connection object and connect to a SQL database.
        this.datasourceSimulator = new DatasourceSimulator(this.connectionConfig.getTagCount(), signalPeriod);

        if (this.datasourceSimulator.connect()) {
            // If the connection is successful, transition to the CONNECTED state. The monitor() function will then
            // be called periodically to ensure the connection is "live".
            this.connectionService.setConnectionState(ConnectionState.CONNECTED);
        } else {
            // If the connection is unsuccessful, transition to the DISCONNECTED state. This connect() function will
            // be called periodically to attempt to connect again.
            this.connectionService.setConnectionState(ConnectionState.DISCONNECTED);
        }
    }

    @Override
    public boolean monitor() {
        // This function will be called periodically to ensure the connection is "live". Do whatever makes sense for
        // your datasource.
        // If the connection is dead, return false. This will cause disconnect() to be called so you can clean
        // up resources and transition to DISCONNECTED.
        return this.datasourceSimulator.isConnected();
    }

    @Override
    public void disconnect() {
        // Transition to the disconnected state.
        this.connectionService.setConnectionState(ConnectionState.DISCONNECTED);

        // Do whatever is necessary to clean up your connection and free up allocated resources.
        this.datasourceSimulator.disconnect();
    }

    @Override
    public void index(SyncMode syncMode) {
        String rootAssetId = this.createRootAsset();

        // Do whatever is necessary to generate the list of signals you want to show up in Seeq. It is generally
        // preferable to use a "streaming" method of iterating through the tags. I.e., try not to hold them all in
        // memory because it is harder to scale to indexing hundreds of thousands of signals. Although these examples
        // use Iterators (which can also be composed in a 'lazy' manner), you may want to consider using Java 8's
        // Streams, which are often friendlier and more convenient to use.

        // Loop through all the tags in our simulated datasource and tell Seeq Server about them
        Iterator<DatasourceSimulator.Tag> tags = this.datasourceSimulator.getTags();
        while (tags.hasNext()) {
            DatasourceSimulator.Tag tag = tags.next();
            String tagId = String.format("%d", tag.getId());

            this.createChildAsset(rootAssetId, tagId, tag.getName());

            SignalWithIdInputV1 signal = new SignalWithIdInputV1();

            // The Data ID is a string that is unique within the data source, and is used by Seeq when referring
            // to signal/asset data. It is important that the Data ID be consistent across connections which means
            // that transient values like generated GUID/UUIDs or the Datasource name would not be ideal. The
            // Data ID is a string and does not need to be numeric, even though we are just using a number in
            // this example.
            signal.setDataId(tagId);

            // The Name is a string that is displayed in the UI. It can change (typically as a result of a
            // rename operation happening in the source system), but the unique Data ID preserves appropriate
            // linkages.
            signal.setName(tag.getName());

            // The interpolation method is the final piece of critical information for a signal.
            signal.setInterpolationMethod(tag.getStepped()
                    ? DatasourceConnectionServiceV2.InterpolationMethod.Step
                    : DatasourceConnectionServiceV2.InterpolationMethod.Linear);

            // putSignal() queues items up for performance reasons and writes them in batch to the server.
            //
            // If you need the signals to be written to Seeq Server before any other work continues, you can
            // call flushSignals() on the connection service.
            this.connectionService.putSignal(signal);

            ConditionUpdateInputV1 condition = new ConditionUpdateInputV1();

            // The Data ID is a string that is unique within the data source, and is used by Seeq when referring
            // to condition data. It is important that the Data ID be consistent across connections which means
            // that transient values like generated GUID/UUIDs or the Datasource name would not be ideal. The
            // Data ID is a string and does not need to be numeric, even though we are just using a number in
            // this example.
            condition.setDataId(String.format("%d", tag.getId()));

            // The Name is a string that is displayed in the UI. It can change (typically as a result of a
            // rename operation happening in the source system), but the unique Data ID preserves appropriate
            // linkages.
            condition.setName(tag.getName());

            // PutCondition() queues items up for performance reasons and writes them in batch to the server.
            //
            // If you need the conditions to be written to Seeq Server before any other work continues, you can
            // call FlushConditions() on the connection service.
            this.connectionService.putCondition(condition);
        }
    }

    @Override
    public Stream<Sample> getSamples(GetSamplesParameters parameters) {
        // Return a stream to iterate through all the samples in the time range.
        //
        // Very important: You must return one sample 'on or earlier' than the requested interval and one sample 'on or
        // later' (if such samples exist). This allows Seeq to interpolate appropriately to the edge of the requested
        // time range.
        //
        // Streams are important to use here to avoid bringing all the data into memory to satisfy the
        // request. The Seeq connector host will automatically "page" the data upload so that we don't hit memory
        // ceilings on large requests. Streams can be created in a variety of ways, such as Guava's
        // Streams.stream(iterable), Java's Stream.of(T... values), or Collection.stream().
        //
        // The code within this function is largely specific to the simulator example. But it should give you an idea of
        // some of the concerns you'll need to attend to.
        return LongStream.rangeClosed(
                        LongMath.divide(parameters.getStartTime().getTimestamp(), this.samplePeriod.toNanos(),
                                RoundingMode.FLOOR),
                        LongMath.divide(parameters.getEndTime().getTimestamp(), this.samplePeriod.toNanos(),
                                RoundingMode.CEILING))
                .boxed()
                .map(sampleIndex -> {
                    TimeInstant key = new TimeInstant(sampleIndex * this.samplePeriod.toNanos());
                    double value = this.datasourceSimulator.query(Waveform.SINE, key.getTimestamp());

                    return new Sample().key(key).value(value);
                })
                .limit(parameters.getSampleLimit())
                .onClose(() -> {
                    // If you have any cleanup to do, do it in this onClose block. This is guaranteed to be called if
                    // iteration is short-circuited for any reason.
                });
    }

    @Override
    public Integer getMaxConcurrentRequests() {
        // This parameter can help control the load that Seeq puts on an external datasource. It is typically
        // controlled from the configuration file.
        return this.connectionConfig.getMaxConcurrentRequests();
    }

    @Override
    public Integer getMaxResultsPerRequest() {
        // This parameter can help control the load and memory usage that Seeq puts on an external datasource. It is
        // typically controlled from the configuration file.
        return this.connectionConfig.getMaxResultsPerRequest();
    }

    @Override
    public Stream<Capsule> getCapsules(GetCapsulesParameters parameters) throws Exception {
        try {
            // This is an example of how you may query your datasource for tag values and is specific to the
            // simulator example. This should be replaced with a call to your own datasource-specific call.
            Iterator<DatasourceSimulator.TagValue> tagValues = this.datasourceSimulator.query(
                    parameters.getDataId(),
                    parameters.getStartTime(),
                    parameters.getEndTime(),
                    parameters.getCapsuleLimit()
            );

            // Return a Stream to iterate through all the capsules in the time range.
            //
            // Streams are important to use here to avoid bringing all the data into memory to satisfy the
            // request. The Seeq connector host will automatically "page" the data upload so that we don't hit memory
            // ceilings on large requests. Streams can be created in a variety of ways, such as Guava's
            // Streams.stream(iterable), Java's Stream.of(T... values), or Collection.stream().
            //
            // The code within this function is largely specific to the simulator example. But it should give you an idea of
            // some of the concerns you'll need to attend to.
            Stream.Builder<Capsule> streamBuilder = Stream.builder();

            while (tagValues.hasNext()) {
                DatasourceSimulator.TagValue tagValue = tagValues.next();

                TimeInstant start = new TimeInstant(tagValue.getStart());
                TimeInstant end = new TimeInstant(tagValue.getEnd());

                List<Capsule.Property> capsuleProperties = new ArrayList<>();
                capsuleProperties.add(new Capsule.Property("Value", Double.toString(tagValue.getValue()), "rads"));

                streamBuilder.accept(new Capsule(start, end, capsuleProperties));
            }

            return streamBuilder.build();
        } finally {
            // If you have any cleanup to do, do it in this finally block. This is guaranteed to be called if
            // iteration is short-circuited for any reason.
        }
    }

    @Override
    public void saveConfig() {
        // Configuration persistence is typically managed by the connector, which stores a list of all connection
        // configurations.
        this.connector.saveConfig();
    }

    // An asset tree is exactly what it sounds like; a tree that describes your asset hierarchies and the relationships
    // between them. This means there needs to be a starting point; a root. This example shows how to create the root
    // asset in the Seeq database.
    private String createRootAsset() {
        String datasourceDataId = this.connectionService.getDatasource().getId();

        AssetInputV1 rootAsset = new AssetInputV1();
        rootAsset.setDataId(datasourceDataId);
        rootAsset.setName("My Datasource Name");
        this.connectionService.putRootAsset(rootAsset);

        return rootAsset.getDataId();
    }

    // To extend the asset tree, a child asset can be created, this examples shows how to do that. To complete the process,
    // a relationship needs to be established between the created asset an it's parent which this example also demonstrates.
    private void createChildAsset(String parentDataId, String childDataId, String childAssetName) {
        // create the child asset
        AssetInputV1 childAsset = new AssetInputV1();
        childAsset.setDataId(childDataId);
        childAsset.setName(childAssetName);
        this.connectionService.putAsset(childAsset);

        // create the child asset relationship to its parent
        AssetTreeSingleInputV1 relationship = new AssetTreeSingleInputV1();
        relationship.setChildDataId(childDataId);
        relationship.setParentDataId(parentDataId);
        this.connectionService.putRelationship(relationship);
    }
}
