package com.mycompany.seeq.link.connector;

import com.seeq.link.sdk.DefaultPullDatasourceConnectionConfig;

/**
 * The configuration object should be a Plain Old Java Object (POJO) with little to no logic, just fields.
 */
public class MyConnectionConfigV1 extends DefaultPullDatasourceConnectionConfig {
    private Integer tagCount;
    private String samplePeriod;

    public Integer getTagCount() {
        return this.tagCount;
    }

    public void setTagCount(int tagCount) {
        this.tagCount = tagCount;
    }

    public String getSamplePeriod() {
        return this.samplePeriod;
    }

    public void setSamplePeriod(String samplePeriod) {
        this.samplePeriod = samplePeriod;
    }
}
