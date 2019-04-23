package com.snowball.location.transport_api.exception;

public class PlaceNotFoundException extends Exception{
    public PlaceNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
