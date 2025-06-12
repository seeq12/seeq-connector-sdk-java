package com.mycompany.seeq.link.connector;

import com.seeq.link.sdk.DefaultPullDatasourceConnectionConfig;

/**
 * The configuration object should be a Plain Old Java Object (POJO) with little to no logic, just fields.
 */
public class MyConnectionConfigV1 extends DefaultPullDatasourceConnectionConfig {
    private String samplePeriod;

    public String getSamplePeriod() {
        return this.samplePeriod;
    }

    public void setSamplePeriod(String samplePeriod) {
        this.samplePeriod = samplePeriod;
    }
}
