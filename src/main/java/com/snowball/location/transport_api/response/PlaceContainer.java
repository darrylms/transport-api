package com.snowball.location.transport_api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceContainer {

    @JsonProperty("request_time")
    @Getter @Setter protected String requestTime;

    @Getter @Setter protected String source;
    @Getter @Setter protected String acknowledgements;

    @Getter @Setter protected String status;

    @JsonProperty("member")
    @Getter @Setter protected List<Place> placees = new ArrayList<>();

    public List<Place> getPlacesByAccuracy() {
        return placees.stream().sorted(Comparator.comparing(Place::getAccuracy)).collect(Collectors.toList());
    }
}
