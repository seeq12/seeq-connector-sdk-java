package com.mycompany.seeq.link.connector;

import java.util.List;

import com.seeq.link.sdk.ConfigObject;
import com.seeq.link.sdk.testframework.IndexingConnectionTestSuite;

public class MyConnectorSignalStandardTest extends IndexingConnectionTestSuite<MyConnection, MyConnector,
        MyConnectionConfigV1, MyConnectorConfigV1> {
    private MyConnector myConnector;
    private MyConnection myConnection;

    @Override
    public void indexingConnectionOneTimeSetUp() {

    }

    @Override
    public MyConnection getConnection() {
        return myConnection;
    }

    @Override
    public MyConnector getConnector() {
        return myConnector;
    }

    @Override
    public List<IgnoredTest> getIgnoredTests() {
        return List.of();
    }

    @Override
    public void baseConnectionOneTimeSetUp() {

    }

    @Override
    public List<ConfigObject> getConnectorConfigVersions() {
        return List.of(new MyConnectorConfigV1());
    }
}
