package com.snowball.location.transport_api;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.snowball.location.transport_api.exception.PlaceNotFoundException;
import com.snowball.location.transport_api.request.RequestBuilder;
import com.snowball.location.transport_api.response.GooglePlace;
import com.snowball.location.transport_api.response.Place;
import com.snowball.location.transport_api.response.PlaceContainer;
import com.snowball.location.transport_api.response.PublicJourneyContainer;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransportService {

    public static final String PLACE_ENDPOINT = "https://transportapi.com/v3/uk/places.json";
    public static final String PUBLIC_JOURNEY_ENDPOINT = "https://transportapi.com/v3/uk/public/journey/from/{from}/to/{to}.json";
    @Setter protected String placeEndpoint = "";
    @Setter protected String publicJourneyEndpoint = "";
    protected String appId;
    protected String appKey;

    public TransportService() {}

    public TransportService(String appId, String appKey) {
        this.appId = appId;
        this.appKey = appKey;
    }

    public Place findPlace(String query, Optional<List<String>> types) throws UnirestException, PlaceNotFoundException {
        String formattedTypes = types.map(type -> String.join(",", type)).orElse(String.join(",", Place.ALL_TYPES));
        Optional<Map<String, String>> queryParams = Optional.of(ImmutableMap.of(
                "type", formattedTypes,
                "app_id", appId,
                "app_key", appKey
        ));
        final RequestBuilder request = RequestBuilder.builder()
                .endpoint(getPlaceEndpoint())
                .queryParams(queryParams)
                .build();

        HttpResponse<PlaceContainer> response = request.get(PlaceContainer.class);
        PlaceContainer placeContainer = response.getBody();
        return placeContainer.getPlacesByAccuracy().stream()
                .sorted(Comparator.comparing(Place::getAccuracy).reversed())
                .findFirst().orElseThrow(() -> new PlaceNotFoundException("Unable to find place " + query));
    }

    public PublicJourneyContainer findJourney(GooglePlace from, GooglePlace to) throws UnirestException {
        return findByLatLong(from, to, Place.ALL_TYPES);
    }

    public PublicJourneyContainer findByLatLong(GooglePlace from, GooglePlace to, List<String> types) throws UnirestException {
        Optional<Map<String, String>> routeParams = Optional.of(ImmutableMap.of(
                "from", "lonlat:"+from.getGeometry().getLocation().getLng()+","+from.getGeometry().getLocation().getLat(),
                "to", "lonlat:"+to.getGeometry().getLocation().getLng()+","+to.getGeometry().getLocation().getLat()
        ));
        Optional<Map<String, String>> queryParams = Optional.of(ImmutableMap.of(
                "type", String.join(",", types),
                "app_id", appId,
                "app_key", appKey
        ));
        final RequestBuilder request = RequestBuilder.builder()
                .endpoint(getPublicJourneyEndpoint())
                .routeParams(routeParams)
                .queryParams(queryParams)
                .build();
        HttpResponse<PublicJourneyContainer> response = request.get(PublicJourneyContainer.class);
        return response.getBody();
    }

    public String getPlaceEndpoint() {
        return (placeEndpoint == null || placeEndpoint.isEmpty()) ? PLACE_ENDPOINT : placeEndpoint;
    }

    public String getPublicJourneyEndpoint() {
        return (publicJourneyEndpoint == null || publicJourneyEndpoint.isEmpty()) ? PUBLIC_JOURNEY_ENDPOINT : publicJourneyEndpoint;
    }
}
