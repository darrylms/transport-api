package com.snowball.location.transport_api;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.snowball.location.transport_api.exception.PlaceNotFoundException;
import com.snowball.location.transport_api.request.RequestBuilder;
import com.snowball.location.transport_api.response.GooglePlace;
import com.snowball.location.transport_api.response.GooglePlaceContainer;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Optional;

public class GoogleMapsService {

    public static final String PLACE_ENDPOINT = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json";
    @Setter protected String placeEndpoint = "";
    @Getter @Setter protected String apiKey;

    public GoogleMapsService() {}

    public GoogleMapsService(String apiKey) {
        this.apiKey = apiKey;
    }

    public GooglePlace findPlace(String query) throws UnirestException, PlaceNotFoundException {
        return findPlace(query, getPlaceEndpoint());
    }

    public GooglePlace findPlace(String query, String endpoint) throws UnirestException, PlaceNotFoundException {
        Optional<Map<String, String>> queryParams = Optional.of(ImmutableMap.of(
                "input",query,
                "inputtype", "textquery",
                "key", apiKey,
                "fields", "formatted_address,name,geometry"
        ));
        final RequestBuilder request = RequestBuilder.builder()
                .endpoint(endpoint)
                .queryParams(queryParams)
                .build();

        HttpResponse<GooglePlaceContainer> response = request.get(GooglePlaceContainer.class);
        GooglePlaceContainer container = response.getBody();
        if (container.hasError()) {
            throw new PlaceNotFoundException("GoogleMapsService could not find place. Status: " + container.getStatus());
        }
        return container.getPlaces().stream().findFirst().orElseThrow(() -> new PlaceNotFoundException("GoogleMapsService returned 0 results"));
    }

    public String getPlaceEndpoint() {
        return (placeEndpoint == null || placeEndpoint.isEmpty()) ? PLACE_ENDPOINT : placeEndpoint;
    }
}
