package com.snowball.location.transport_api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Place {

    protected static final String TYPE_TRAIN_STATION = "train_station";
    protected static final String TYPE_BUS_STOP = "bus_stop";
    protected static final String TYPE_SETTLEMENT = "settlement";
    protected static final String TYPE_REGION = "region";
    protected static final String TYPE_STREET = "street";
    protected static final String TYPE_POI = "poi"; //PlaceContainer of interest
    protected static final String TYPE_POSTCODE = "postcode";

    public static final List<String> ALL_TYPES = Collections.unmodifiableList(Arrays.asList(
            TYPE_TRAIN_STATION, TYPE_BUS_STOP, TYPE_SETTLEMENT, TYPE_REGION, TYPE_STREET, TYPE_POI, TYPE_POSTCODE));

    public static final List<String> PLACE_TYPES = Collections.unmodifiableList(Arrays.asList(
       TYPE_POSTCODE, TYPE_SETTLEMENT, TYPE_POI, TYPE_STREET
    ));

    @Getter @Setter protected String type;
    @Getter @Setter protected String name;
    @Getter @Setter protected String description;
    @Getter @Setter protected String latitude;
    @Getter @Setter protected String longitude;
    @Getter @Setter protected int accuracy;

    @JsonProperty("atcocode")
    @Getter @Setter protected String atcoCode;
    @Getter @Setter protected String distance;

    @JsonProperty("station_code")
    @Getter @Setter protected String stationCode;

    @JsonProperty("tiploc_code")
    @Getter @Setter protected String tiplocCode;

    @JsonProperty("osm_id")
    @Getter @Setter protected String osmId;
}
