package com.mycompany.seeq.link.connector;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.seeq.link.sdk.ConfigObject;
import com.seeq.link.sdk.interfaces.ConnectorServiceV2;
import com.seeq.link.sdk.testframework.SignalPullConnectionTestSuite;

public class MyConnectorSignalStandardTest extends SignalPullConnectionTestSuite<MyConnection, MyConnector,
        MyConnectionConfigV1, MyConnectorConfigV1> {
    private MyConnector myConnectorMock;
    private MyConnection myConnection;

    private final Map<StandardTest, String> DATA_IDS_FOR_STANDARD_TESTS = Map.of(
            StandardTest.NoSamplesOutsideBoundary, "signal-data-id-1",
            StandardTest.SampleOneNanosecondAfterEnd, "signal-data-id-2",
            StandardTest.SampleOneNanosecondBeforeEnd, "signal-data-id-4",
            StandardTest.SampleOneNanosecondBeforeStart, "signal-data-id-5",
            StandardTest.SampleOnLeftBoundary, "signal-data-id-6",
            StandardTest.SampleOnRightBoundary, "signal-data-id-7",
            StandardTest.SamplesOutsideBoundaryOnly, "signal-data-id-8"
    );

    private final Map<String, String> DATA_IDS_FOR_CUSTOM_TESTS = Map.of(
            "ConnectionWithoutTagCount", "signal-data-id-9",
            "ConnectionWithInvalidSamplePeriod", "signal-data-id-10"
    );

    /*
     * Use this method to provide the data ID to be used for each standard test in the suite. You can follow the
     * style used here or keep the determination logic inline if you'd prefer. Ensure that data IDs are provided for every Standard test not skipped as well as for every Custom test defined.
     *
     * NOTE: the names for custom tests should match the test data file name exactly to avoid errors.
     */
    @Override
    public String dataIdForTest(String testName) {
        if (DATA_IDS_FOR_CUSTOM_TESTS.containsKey(testName)) {
            return DATA_IDS_FOR_CUSTOM_TESTS.get(testName);
        }

        return DATA_IDS_FOR_STANDARD_TESTS.get(StandardTest.valueOf(testName));
    }

    @Override
    public void signalPullConnectionOneTimeSetUp() {
    }

    @Override
    public void pullConnectionOneTimeSetUp() {
    }

    @Override
    public void indexingConnectionOneTimeSetUp() {
    }

    @Override
    public MyConnection getConnection() {
        return myConnection;
    }

    @Override
    public MyConnector getConnector() {
        return myConnectorMock;
    }

    @Override
    public List<IgnoredTest> getIgnoredTests() {
        /*
         * We are using a shared reason because we are testing a simulated datasource. If you choose to ignore a
         * test against a real datasource, you will need to give individual, detailed reasons for each skipped test
         */
        final String sharedIgnoreReason = "Our simulated datasource only returns floating point values";
        return List.of(
                new IgnoredTest(StandardTest.BooleanValuedSamples, sharedIgnoreReason),
                new IgnoredTest(StandardTest.EnumerationValuedSamples, sharedIgnoreReason),
                new IgnoredTest(StandardTest.IntegerValuedSamples, sharedIgnoreReason),
                new IgnoredTest(StandardTest.StringValuedSamples, sharedIgnoreReason),
                new IgnoredTest(StandardTest.MultivaluedSamples, "The simulated datasource does not support multivalue samples"),
                new IgnoredTest(StandardTest.NoSamplesAtAll,
                        "The simulated datasource will always return at least one sample"),
                new IgnoredTest(StandardTest.SampleOneNanosecondAfterStart,
                        "The simulated datasource cannot generate samples one nanosecond after start")
        );
    }

    /*
     * Use this method to configure the connector and connection that should be used for all standard tests in
     * the suite
     */
    @Override
    public void baseConnectionOneTimeSetUp() {
        var connectionConfig = new MyConnectionConfigV1();
        connectionConfig.setSamplePeriod("1s");
        connectionConfig.setTagCount(5);
        connectionConfig.setEnabled(true);

        var connectorConfig = new MyConnectorConfigV1();
        connectorConfig.setConnections(List.of(connectionConfig));

        try {
            var mockConnectorService = mock(ConnectorServiceV2.class);
            when(mockConnectorService.loadConfig(any())).thenReturn(connectorConfig);
            when(mockConnectorService.log()).thenReturn(mock());

            this.myConnectorMock = mock();
            myConnection = new MyConnection(this.myConnectorMock, connectionConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ConfigObject> getConnectorConfigVersions() {
        return List.of(new MyConnectorConfigV1());
    }
}
