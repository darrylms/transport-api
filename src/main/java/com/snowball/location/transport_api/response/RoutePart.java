package com.snowball.location.transport_api.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class RoutePart {

    public static final String MODE_FOOT = "foot";
    public static final String MODE_TUBE = "tube";
    public static final String MODE_DLR = "dlr";
    public static final String MODE_BUS = "bus";
    public static final String MODE_TRAM = "tram";
    public static final String MODE_TRAIN = "train";
    public static final String MODE_OVERGROUND = "overground";
    public static final String MODE_BOAT = "boat";
    public static final String MODE_WAIT = "wait";
    public static final String MODE_UNKNOWN = "unknown route";

    @Getter @Setter private String mode; //"foot", "tube", "dlr", "bus", "tram", "train", "overground", "boat", "wait", "unknown"

    @JsonProperty("from_point_name")
    @Getter @Setter protected String from;

    @JsonProperty("to_point_name")
    @Getter @Setter protected String to;

    @Getter @Setter protected String destination;

    @JsonProperty("departure_time")
    @Getter @Setter protected String departure;

    @JsonProperty("arrival_time")
    @Getter @Setter protected String arrival;

    @JsonProperty("line_name")
    @Getter @Setter protected String lineName;

    @Getter @Setter protected String duration;

    /* Ignoring for now */
    @JsonIgnore
    @Getter @Setter protected List<String[]> coordinates = new ArrayList<>();
}
