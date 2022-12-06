package com.cebbus.calibrator.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SearchCriteria {

    @JsonProperty("property")
    private String key;

    @JsonProperty("operator")
    private String operation;

    private Object value;

    private boolean caseSensitive;

    public SearchCriteria() {
    }

    public SearchCriteria(String key, String operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }
}
