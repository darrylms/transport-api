package com.snowball.location.transport_api.response;

import lombok.Getter;
import lombok.Setter;

public class GoogleLocation {

    @Getter @Setter protected String lat;
    @Getter @Setter protected String lng;
}
