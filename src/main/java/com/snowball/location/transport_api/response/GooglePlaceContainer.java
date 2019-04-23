package com.snowball.location.transport_api.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class GooglePlaceContainer {

    public static final String STATUS_OK = "OK";
    public static final String STATUS_ZERO_RESULTS = "ZERO_RESULTS";
    public static final String STATUS_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
    public static final String STATUS_REQUEST_DENIED = "REQUEST_DENIED";
    public static final String STATUS_INVALID_REQUEST = "INVALID_REQUEST";
    public static final String STATUS_UNKNOWN_ERROR = "UNKNOWN_ERROR";

    @Getter @Setter protected String status;

    @JsonProperty("candidates")
    @Getter @Setter protected List<GooglePlace> places = new ArrayList<>();

    @JsonIgnore
    public boolean isStatusOk() {
        return status.equals(STATUS_OK);
    }

    @JsonIgnore
    public boolean hasNoResults() {
        return status.equals(STATUS_ZERO_RESULTS);
    }

    @JsonIgnore
    public boolean hasError() {
        return status.equals(STATUS_OVER_QUERY_LIMIT) || status.equals(STATUS_REQUEST_DENIED) || status.equals(STATUS_INVALID_REQUEST) || status.equals(STATUS_UNKNOWN_ERROR);
    }

}
