package com.snowball.location.transport_api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.snowball.location.transport_api.exception.PlaceNotFoundException;
import com.snowball.location.transport_api.response.*;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import static org.junit.jupiter.api.Assertions.*;

public class TestTransportService {

    String placeEndpoint = "http://localhost:8745/uk/places.json";
    String journeyEndpoint = "http://localhost:8745/uk/public/journey/from/{from}/to/{to}.json";

    String mockPlaceEndpoint = "/uk/places.json.*";
    String mockJourneyEndpoint = "/uk/public/journey/from/(.*)/to/(.*).json.*";

    @BeforeAll
    static void setup() {
        initUnirest();
    }

    @Test
    void testFindPlace() throws JsonProcessingException, PlaceNotFoundException, UnirestException {
        Place place1 = new Place();
        place1.setType("train_station");
        place1.setName("London Waterloo");
        place1.setDescription("Waterloo, Crosby");
        place1.setLatitude("test lat");
        place1.setLongitude("test lng");
        place1.setAccuracy(100);
        place1.setStationCode("WAT");
        place1.setTiplocCode("WATRLOO");

        Place place2 = new Place();
        place2.setType("bus_stop");
        place2.setName("Waterloo Station (Stop A) - SW-bound");
        place2.setDescription("Waterloo, Crosby");
        place2.setLatitude("test lat 2");
        place2.setLongitude("test lng 2");
        place2.setAccuracy(20);
        place2.setAtcoCode("2800S47059A");

        PlaceContainer cont = new PlaceContainer();
        cont.setRequestTime("2018-07-20 15:25+01:00");
        cont.setSource("Network Rail, NaPTAN");
        cont.setAcknowledgements("test ack");
        cont.setPlacees(Arrays.asList(place1, place2));

        WireMockServer wireMockServer = setupWireMock(convertToJson(cont), mockPlaceEndpoint);

        TransportService service = new TransportService("appId", "appKey");
        service.setPlaceEndpoint(placeEndpoint);
        Place result = service.findPlace("some place lookup", Optional.of(Place.ALL_TYPES));
        assertEquals(result.getName(), place1.getName());
        assertEquals(result.getAccuracy(), place1.getAccuracy());
        assertEquals(result.getLatitude(), place1.getLatitude());
        assertEquals(result.getLongitude(), place1.getLongitude());
        wireMockServer.stop();
    }

    @Test
    void testNoPlace() throws JsonProcessingException {
        PlaceContainer cont = new PlaceContainer();
        cont.setRequestTime("2018-07-20 15:25+01:00");
        cont.setSource("Network Rail, NaPTAN");
        cont.setAcknowledgements("test ack");

        WireMockServer wireMockServer = setupWireMock(convertToJson(cont), mockPlaceEndpoint);

        TransportService service = new TransportService("appId", "appKey");
        service.setPlaceEndpoint(placeEndpoint);
        PlaceNotFoundException thrown = assertThrows(PlaceNotFoundException.class,
                () -> service.findPlace("some place lookup", Optional.of(Place.ALL_TYPES)));
        assertTrue(thrown.getMessage().contains("Unable to find place "));
        wireMockServer.stop();
    }

    @Test
    void testFindByLatLong() throws JsonProcessingException, UnirestException {
        RoutePart part = new RoutePart();
        part.setMode(RoutePart.MODE_TRAM);
        part.setFrom("Tramp stop 1");
        part.setTo("Tram stop 2");
        part.setDestination("End of the line");
        part.setDeparture("07:33");
        part.setArrival("07:41");
        part.setLineName("");
        part.setDuration("00:08:00");

        RoutePart part2 = new RoutePart();
        part2.setMode(RoutePart.MODE_FOOT);
        part2.setFrom("Tram stop 2");
        part2.setTo("Bus stop 1");
        part2.setDestination("");
        part2.setDeparture("07:41");
        part2.setArrival("07:45");
        part2.setLineName("");
        part2.setDuration("00:04:00");

        RoutePart part3 = new RoutePart();
        part3.setMode(RoutePart.MODE_BUS);
        part3.setFrom("Bus stop 1");
        part3.setTo("Bus stop 2");
        part3.setDestination("Some destination");
        part3.setDeparture("07:45");
        part3.setArrival("08:20");
        part3.setLineName("J45");
        part3.setDuration("00:35:00");

        PublicJourneyRoute route = new PublicJourneyRoute();
        route.setDeparture("07:33");
        route.setArrival("08:20");
        route.setArrivalDate("");
        route.setRouteParts(Arrays.asList(part, part2, part3));

        PublicJourneyContainer cont = new PublicJourneyContainer();
        cont.setRequestTime("07:32");
        cont.setSource("test source");
        cont.setAcknowledgements("test ack");
        cont.setRoutes(Arrays.asList(route));

        GoogleLocation loc = new GoogleLocation();
        loc.setLat("51.50544");
        loc.setLng("-0.09106059999999999");
        GoogleGeometry geo = GoogleGeometry.builder().location(loc).build();
        GooglePlace place = new GooglePlace();
        place.setAddress("address line 1, city postcode, UK");
        place.setName("Place 1");
        place.setGeometry(geo);

        GoogleLocation loc2 = new GoogleLocation();
        loc2.setLat("51.5031653");
        loc2.setLng("-0.1123051");
        GoogleGeometry geo2 = GoogleGeometry.builder().location(loc).build();
        GooglePlace place2 = new GooglePlace();
        place2.setAddress("some other address line 1, city postcode, UK");
        place2.setName("Place 2");
        place2.setGeometry(geo2);

        WireMockServer wireMockServer = setupWireMock(convertToJson(cont), mockJourneyEndpoint);

        TransportService service = new TransportService("appId", "appKey");
        service.setPublicJourneyEndpoint(journeyEndpoint);
        PublicJourneyContainer result = service.findByLatLong(place, place2, Place.ALL_TYPES);
        assertEquals(result.getRoutes().size(), 1);
        PublicJourneyRoute resultRoute = result.getRoutes().stream().findFirst().get();
        assertEquals(resultRoute.getRouteParts().size(), 3);
        assertEquals(resultRoute.getRouteParts().get(0).getMode(), RoutePart.MODE_TRAM);
        assertEquals(resultRoute.getRouteParts().get(1).getMode(), RoutePart.MODE_FOOT);
        assertEquals(resultRoute.getRouteParts().get(2).getMode(), RoutePart.MODE_BUS);
        wireMockServer.stop();
    }

    private String convertToJson(Object cont) throws JsonProcessingException {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = getObjectMapper();
        return objectMapper.writeValueAsString(cont);
    }

    private static com.fasterxml.jackson.databind.ObjectMapper getObjectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .registerModule(new JavaTimeModule())
                .findAndRegisterModules();
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
