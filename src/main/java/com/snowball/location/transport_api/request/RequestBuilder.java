package com.snowball.location.transport_api.request;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import lombok.Builder;
import lombok.NonNull;

import java.util.Map;
import java.util.Optional;

@Builder
public class RequestBuilder {
    @Builder.Default
    Optional<Map<String, String>> routeParams = Optional.empty();
    @Builder.Default
    Optional<Map<String, String>> queryParams = Optional.empty();
    @NonNull String endpoint;

    public <T> HttpResponse<T> get(Class<T> returnType) throws UnirestException {
        GetRequest request = Unirest.get(endpoint);
        routeParams.ifPresent(param -> param.forEach(request::routeParam));
        queryParams.ifPresent(param -> param.forEach(request::queryString));
        try {
            return request.asObject(returnType);
        } catch (UnirestException e) {
            throw new UnirestException(e);
        }
    }

}
