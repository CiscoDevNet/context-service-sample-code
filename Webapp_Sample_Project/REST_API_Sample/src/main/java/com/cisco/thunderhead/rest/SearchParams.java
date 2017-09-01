package com.cisco.thunderhead.rest;

import com.cisco.thunderhead.ExposeMember;
import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.util.RFC3339Date;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to specify search criteria.
 */
public class SearchParams {
    @ExposeMember public String operation;

    @ExposeMember public Map<String,List<String>> query;

    public SearchParams(String operation, Map<String,List<String>> query) {
        this.operation = operation;
        this.query = query;
    }

    public SearchParameters getSearchParameters() {
        if (query==null) return null;

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.putAll(query);
        return searchParameters;
    }

    public Operation getOperation() {
        if (operation==null) {
            return null;
        }

        Operation op;
        switch(operation.toLowerCase()) {
            case "and":
                op = Operation.AND;
                break;
            case "or":
                op = Operation.OR;
                break;
            default:
                op = null;
        }
        return op;
    }
}
