package com.snowball.location.transport_api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.snowball.location.transport_api.exception.PlaceNotFoundException;
import com.snowball.location.transport_api.response.GoogleGeometry;
import com.snowball.location.transport_api.response.GoogleLocation;
import com.snowball.location.transport_api.response.GooglePlace;
import com.snowball.location.transport_api.response.GooglePlaceContainer;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;


import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TestGoogleMapsService {

    String placeEndpoint = "http://localhost:8745/maps/api/place/findplacefromtext/json";
    String mockPlaceEndpoint = "/maps/api/place/findplacefromtext/json.*";

    @BeforeAll
    static void setup() {
        initUnirest();
    }

    @Test
    void testFindPlace() throws JsonProcessingException, PlaceNotFoundException, UnirestException {
        GoogleLocation loc = new GoogleLocation();
        loc.setLat("51.50544");
        loc.setLng("-0.09106059999999999");
        GoogleGeometry geo = GoogleGeometry.builder().location(loc).build();
        GooglePlace place = new GooglePlace();
        place.setAddress("address line 1, city postcode, UK");
        place.setName("Place 1");
        place.setGeometry(geo);
        GooglePlaceContainer cont = new GooglePlaceContainer();
        cont.setStatus(GooglePlaceContainer.STATUS_OK);

        GoogleLocation loc2 = new GoogleLocation();
        loc2.setLat("51.5031653");
        loc2.setLng("-0.1123051");
        GoogleGeometry geo2 = GoogleGeometry.builder().location(loc).build();
        GooglePlace place2 = new GooglePlace();
        place2.setAddress("some other address line 1, city postcode, UK");
        place2.setName("Place 2");
        place2.setGeometry(geo2);

        cont.setPlaces(Arrays.asList(place, place2));

        String query = "test query";
        String apiKey = "test api key";

        WireMockServer wireMockServer = setupWireMock(convertToJson(cont), mockPlaceEndpoint);

        GoogleMapsService service = new GoogleMapsService(apiKey);
        service.setPlaceEndpoint(placeEndpoint);
        GooglePlace result = service.findPlace(query, "http://localhost:8745/maps/api/place/findplacefromtext/json");
        assertEquals(place.getName(), result.getName());
        assertEquals(place.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLat());
        assertEquals(place.getGeometry().getLocation().getLng(), result.getGeometry().getLocation().getLng());
        wireMockServer.stop();
    }

    @Test
    void testPlaceNotFound() throws JsonProcessingException {
        GoogleLocation loc = new GoogleLocation();
        loc.setLat("51.50544");
        loc.setLng("-0.09106059999999999");
        GoogleGeometry geo = GoogleGeometry.builder().location(loc).build();
        GooglePlace place = new GooglePlace();
        place.setAddress("address line 1, city postcode, UK");
        place.setName("Place 1");
        place.setGeometry(geo);
        GooglePlaceContainer cont = new GooglePlaceContainer();
        cont.setStatus(GooglePlaceContainer.STATUS_REQUEST_DENIED);

        GoogleLocation loc2 = new GoogleLocation();
        loc2.setLat("51.5031653");
        loc2.setLng("-0.1123051");
        GoogleGeometry geo2 = GoogleGeometry.builder().location(loc).build();
        GooglePlace place2 = new GooglePlace();
        place2.setAddress("some other address line 1, city postcode, UK");
        place2.setName("Place 2");
        place2.setGeometry(geo2);

        cont.setPlaces(Arrays.asList(place, place2));

        String query = "test query";
        String apiKey = "test api key";

        WireMockServer wireMockServer = setupWireMock(convertToJson(cont), mockPlaceEndpoint);

        GoogleMapsService service = new GoogleMapsService(apiKey);
        PlaceNotFoundException thrown = assertThrows(PlaceNotFoundException.class,
                () -> service.findPlace(query, placeEndpoint),
                "Expected behaviour when the Google Maps API returns an error");
        assertTrue(thrown.getMessage().contains("GoogleMapsService could not find place."));
        wireMockServer.stop();
    }

    @Test
    void testNoResults() throws JsonProcessingException {
        GooglePlaceContainer cont = new GooglePlaceContainer();
        cont.setStatus(GooglePlaceContainer.STATUS_ZERO_RESULTS);

        String query = "test query";
        String apiKey = "test api key";

        WireMockServer wireMockServer = setupWireMock(convertToJson(cont), mockPlaceEndpoint);

        GoogleMapsService service = new GoogleMapsService(apiKey);
        PlaceNotFoundException thrown = assertThrows(PlaceNotFoundException.class,
                () -> service.findPlace(query, placeEndpoint),
                "Expected behaviour when result contains no Places.");
        assertTrue(thrown.getMessage().contains("GoogleMapsService returned 0 results"));
        wireMockServer.stop();
    }

    private static com.fasterxml.jackson.databind.ObjectMapper getObjectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .registerModule(new JavaTimeModule())
                .findAndRegisterModules();
    }

    private String convertToJson(Object cont) throws JsonProcessingException {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = getObjectMapper();
        return objectMapper.writeValueAsString(cont);
    }

    private WireMockServer setupWireMock(String serverResponse, String endpoint) {
        WireMockServer wireMockServer = new WireMockServer(options().port(8745));
        configureFor("localhost", 8745);
        wireMockServer.stubFor(get(urlPathMatching(endpoint))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(serverResponse)));
        wireMockServer.start();
        return wireMockServer;
    }

    private static void initUnirest() {
        Unirest.setObjectMapper(new ObjectMapper() {
            //Ensure Jackson modules to handle Java 8 datatypes are loaded.
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = getObjectMapper();
            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                            = new com.fasterxml.jackson.databind.ObjectMapper();
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Unirest.setHttpClient(HttpClientBuilder.create()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                .build());
    }
}
