package com.seeq.link.connector.flexgen.config;

import com.seeq.link.sdk.DefaultPullDatasourceConnectionConfig;

/**
 * The configuration object should be a Plain Old Java Object (POJO) with little to no logic, just fields.
 */
public class MongoDbConnectionConfigV1 extends DefaultPullDatasourceConnectionConfig {
    private String _databaseName = "";
    private String _connectionString = "";
    private String _fieldForConditionName = "source";

    private String _timestampField = "time";
    private String _collectionName = "events";
    private int _conditionDuration = 5;
    public String getDatabaseName() {
        return this._databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this._databaseName = databaseName;
    }

    public String getFieldForConditionName() {
        return this._fieldForConditionName;
    }

    public void setFieldForConditionName(String fieldForConditionNamed) {
        this._fieldForConditionName = fieldForConditionNamed;
    }
    public String getCollectionName() {
        return this._collectionName;
    }

    public void setCollectionName(String collectionName) {
        this._collectionName = collectionName;
    }

    public int getConditionDuration(){
        return _conditionDuration;
    }

    public void setConditionDuration(int conditionDuration){
        _conditionDuration = conditionDuration;
    }

    public String getTimestampField(){
        return _timestampField;
    }

    public void setTimestampField(String timestampField){
        _timestampField = timestampField;
    }

    public String getConnectionString(){
        return _connectionString;
    }

    public void setConnectionString(String connectionString){
        _connectionString = connectionString;
    }


}
