package com.mycompany.seeq.link.connector;

import java.util.ArrayList;
import java.util.List;

import com.seeq.model.ScalarPropertyV1;

/**
 * Stub used in tests to supply a datasource with additional properties (e.g. Data Version Check).
 * The Seeq Link SDK's getDatasource() may return a type that has getId() and getAdditionalProperties();
 * this stub implements that contract for testing.
 */
public class DatasourceStub {
    private String id = "test-datasource-id";
    private final List<ScalarPropertyV1> additionalProperties = new ArrayList<>();

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ScalarPropertyV1> getAdditionalProperties() {
        return this.additionalProperties;
    }
}
