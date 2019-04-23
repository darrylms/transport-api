package com.snowball.location.transport_api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Builder(
        toBuilder = true
)
@AllArgsConstructor
@NoArgsConstructor(
        force = true,
        access = AccessLevel.PACKAGE
)
@Value
@JsonIgnoreProperties("viewport")
public class GoogleGeometry {

    @Getter @Setter protected GoogleLocation location;

}
