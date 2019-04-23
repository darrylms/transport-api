package com.snowball.location.transport_api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties({"opening_hours", "photos", "rating"})
public class GooglePlace {

    @JsonProperty("formatted_address")
    @Getter @Setter protected String address;
    @Getter @Setter protected GoogleGeometry geometry;
    @Getter @Setter protected String name;

}
