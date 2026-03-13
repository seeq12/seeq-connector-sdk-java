package com.mycompany.seeq.link.connector;

import com.seeq.link.sdk.DefaultPullDatasourceConnectionConfig;

/**
 * The configuration object should be a Plain Old Java Object (POJO) with little to no logic, just fields.
 */
public class MyConnectionConfigV1 extends DefaultPullDatasourceConnectionConfig {
    private String samplePeriod;

    /**
     * How often to run incremental indexing (e.g. "1h", "30m"). When set, the SDK uses this to
     * schedule incremental index runs between full index runs. Omit or null for full-only indexing.
     */
    private String incrementalIndexingFrequency = "1h";

    /**
     * Maximum number of changed items per database to process in one incremental index run.
     * If the number of changes exceeds this value, incremental indexing is skipped for that
     * database and a full index will be performed on the next run.
     */
    private int incrementalIndexingMaxChangedPerDatabase = 10_000;

    public String getSamplePeriod() {
        return this.samplePeriod;
    }

    public void setSamplePeriod(String samplePeriod) {
        this.samplePeriod = samplePeriod;
    }

    public String getIncrementalIndexingFrequency() {
        return this.incrementalIndexingFrequency;
    }

    public void setIncrementalIndexingFrequency(String incrementalIndexingFrequency) {
        this.incrementalIndexingFrequency = incrementalIndexingFrequency;
    }

    public int getIncrementalIndexingMaxChangedPerDatabase() {
        return this.incrementalIndexingMaxChangedPerDatabase;
    }

    public void setIncrementalIndexingMaxChangedPerDatabase(int incrementalIndexingMaxChangedPerDatabase) {
        this.incrementalIndexingMaxChangedPerDatabase = incrementalIndexingMaxChangedPerDatabase;
    }
}
