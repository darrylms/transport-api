package com.snowball.location.transport_api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.snowball.location.transport_api.exception.PlaceNotFoundException;
import com.snowball.location.transport_api.response.GooglePlace;
import com.snowball.location.transport_api.response.PublicJourneyContainer;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.pmw.tinylog.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;

public class App {
    public static void main(String args[]) throws UnirestException, PlaceNotFoundException {
        Properties prop = readProperties();
        // Add your own credentials to test
        String appId = prop.getProperty("transportApi.appId");
        String appKey = prop.getProperty("transportApi.appKey");
        String apiKey = prop.getProperty("googleMaps.apiKey");

        initUnirest();
        TransportService service = new TransportService(appId, appKey);
        GoogleMapsService google = new GoogleMapsService(apiKey);
        GooglePlace place = google.findPlace("london borough market");
        GooglePlace place2 = google.findPlace("Waterloo station");
        PublicJourneyContainer journey = service.findJourney(place, place2);
        if (journey.getRoutes().isEmpty()) {
            System.out.println("No routes in Journey");
            return;
        }
        String result = journey.getRoutes().stream()
                .findFirst().map(route -> route.getRouteParts().stream()
                    .map(part -> part.getMode() + ": From " + part.getFrom() + " to " + part.getTo())
                    .collect(Collectors.joining("\r\n"))).orElseThrow(() -> new PlaceNotFoundException("Something went wrong"));
        Logger.info(result);
    }

    public static Properties readProperties() {
        Properties prop = new Properties();
        InputStream input;
        try {
            String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            input = new FileInputStream(rootPath + "application.properties");
            prop.load(input);
        } catch (IOException e) {
            Logger.error(e);
        }
        return prop;
    }

    private static void initUnirest() {
        Unirest.setObjectMapper(new ObjectMapper() {
            //Ensure Jackson modules to handle Java 8 datatypes are loaded.
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper()
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                    .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                    .registerModule(new JavaTimeModule())
                    .findAndRegisterModules();
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
