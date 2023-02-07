package com.mycompany.seeq.link.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.slf4j.Logger;

import com.seeq.link.sdk.interfaces.DatasourceConnectionServiceV2;
import com.seeq.link.sdk.interfaces.GetSamplesParameters;
import com.seeq.link.sdk.utilities.Sample;
import com.seeq.link.sdk.utilities.TimeInstant;

public class MyConnectionTest {
    @Test
    public void getSamples() {
        MyConnectionConfigV1 config = new MyConnectionConfigV1();

        config.setEnabled(true);
        config.setSamplePeriod("1s");
        config.setTagCount(100);

        DatasourceConnectionServiceV2 connectionServiceMock = mock(DatasourceConnectionServiceV2.class);
        when(connectionServiceMock.log()).thenReturn(mock(Logger.class));

        MyConnection connection = new MyConnection(null, config);
        connection.initialize(connectionServiceMock);
        connection.connect();

        assertThat(connection.getSamples(new GetSamplesParameters("MyDataId1",
                new TimeInstant(2 * 1_000_000_000L - 100), new TimeInstant(4 * 1_000_000_000L + 100), 0, 10, null, "")))
                .containsExactly(
                        new Sample().key(new TimeInstant(1_000_000_000L)).value(0.06279051952931337),
                        new Sample().key(new TimeInstant(2_000_000_000L)).value(0.12533323356430426),
                        new Sample().key(new TimeInstant(3_000_000_000L)).value(0.1873813145857246),
                        new Sample().key(new TimeInstant(4_000_000_000L)).value(0.2486898871648548),
                        new Sample().key(new TimeInstant(5_000_000_000L)).value(0.3090169943749474));

        // Ensure sampleLimit works
        assertThat(connection.getSamples(new GetSamplesParameters("MyDataId2",
                new TimeInstant(2 * 1_000_000_000L - 100), new TimeInstant(4 * 1_000_000_000L + 100), 0, 2, null, "")))
                .containsExactly(
                        new Sample().key(new TimeInstant(1_000_000_000L)).value(0.06279051952931337),
                        new Sample().key(new TimeInstant(2_000_000_000L)).value(0.12533323356430426));
    }

}
