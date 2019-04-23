package com.snowball.location.transport_api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class PublicJourneyContainer {
    @JsonProperty("request_time")
    @Getter @Setter protected String requestTime;

    @Getter @Setter protected String source;
    @Getter @Setter protected String acknowledgements;

    @Getter @Setter protected List<PublicJourneyRoute> routes = new ArrayList<>();

    @Getter @Setter protected String message;
}
