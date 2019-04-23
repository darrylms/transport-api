package com.snowball.location.transport_api.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class PublicJourneyRoute {

    @JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "hh:mm:ss")
    @Getter @Setter protected String duration;

    @JsonProperty("departure_time")
    @Getter @Setter protected String departure;

    @JsonProperty("arrival_time")
    @Getter @Setter protected String arrival;

    @JsonProperty("arrival_date")
    @Getter @Setter protected String arrivalDate;

    @JsonProperty("route_parts")
    @Getter @Setter protected List<RoutePart> routeParts = new ArrayList<>();

}
