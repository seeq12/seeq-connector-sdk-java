package com.seeq.link.connector.flexgen;

import com.seeq.link.connector.flexgen.config.MongoDbConnectionConfigV1;
import com.seeq.link.sdk.interfaces.Connection;
import com.seeq.link.sdk.interfaces.DatasourceConnectionServiceV2;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MongoDbConnectionTest {
    @Test
    public void testGetParentId() {
        MongoDbConnectionConfigV1 config = new MongoDbConnectionConfigV1();

        config.setEnabled(true);

        MongoDbConnection connection = new MongoDbConnection(null, config);
        String parent = connection.getParentAsset("something:somethingElse");
        assertThat(parent).isEqualTo("somethingElse");
    }

    @Test
    public void testConnectInvalidConfigurationDoesNotEnable() {
        MongoDbConnectionConfigV1 config = new MongoDbConnectionConfigV1();

        config.setEnabled(true);
        config.setTimestampField("");
        config.setFieldForConditionName("");
        DatasourceConnectionServiceV2 connectionServiceMock = mock(DatasourceConnectionServiceV2.class);
        Logger mockLogger = mock(Logger.class);
        when(connectionServiceMock.log()).thenReturn(mockLogger);

        MongoDbConnection connection = new MongoDbConnection(null, config);
        connection.initialize(connectionServiceMock);
        verify(connectionServiceMock, times(0)).enable();
        verify(mockLogger, times(1)).error(Mockito.contains("fieldForConditionName"));
    }

    @Test
    public void testValidConfigurationEnablesConnection() {
        MongoDbConnectionConfigV1 config = new MongoDbConnectionConfigV1();
        config.setConnectionString("mongodb://localhost:9999/?serverSelectionTimeoutMS=1000");
        config.setDatabaseName("testDb");
        config.setEnabled(true);
        DatasourceConnectionServiceV2 connectionServiceMock = mock(DatasourceConnectionServiceV2.class);
        Logger mockLogger = mock(Logger.class);
        when(connectionServiceMock.log()).thenReturn(mockLogger);

        MongoDbConnection connection = new MongoDbConnection(null, config);
        connection.initialize(connectionServiceMock);
        verify(connectionServiceMock, times(1)).enable();
    }

    @Test
    public void testRespectEnabledFlagInInitialize() {
        MongoDbConnectionConfigV1 config = new MongoDbConnectionConfigV1();

        config.setEnabled(false);
        DatasourceConnectionServiceV2 connectionServiceMock = mock(DatasourceConnectionServiceV2.class);
        Logger mockLogger = mock(Logger.class);
        when(connectionServiceMock.log()).thenReturn(mockLogger);

        MongoDbConnection connection = new MongoDbConnection(null, config);
        connection.initialize(connectionServiceMock);
        verify(connectionServiceMock, times(0)).enable();
    }

    @Test
    public void testNoConnect() {
        MongoDbConnectionConfigV1 config = new MongoDbConnectionConfigV1();

        config.setEnabled(false);
        config.setDatabaseName("testDb");
        config.setConnectionString("mongodb://localhost:9999/?serverSelectionTimeoutMS=1000");

        DatasourceConnectionServiceV2 connectionServiceMock = mock(DatasourceConnectionServiceV2.class);
        Logger mockLogger = mock(Logger.class);
        when(connectionServiceMock.log()).thenReturn(mockLogger);

        MongoDbConnection connection = new MongoDbConnection(null, config);
        connection.initialize(connectionServiceMock);
        connection.connect();
        verify(connectionServiceMock, times(0)).setConnectionState(eq(Connection.ConnectionState.CONNECTED));
        verify(mockLogger, times(1)).error(Mockito.contains("Error connecting to cluster."));
    }


}
