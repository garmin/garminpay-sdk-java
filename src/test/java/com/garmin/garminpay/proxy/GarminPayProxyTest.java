/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garmin.garminpay.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garmin.garminpay.TestUtils;
import com.garmin.garminpay.client.RefreshableOauthClient;
import com.garmin.garminpay.exception.GarminPayApiException;
import com.garmin.garminpay.model.dto.APIResponseDTO;
import com.garmin.garminpay.model.response.ErrorResponse;
import com.garmin.garminpay.model.response.ExchangeKeysResponse;
import com.garmin.garminpay.model.response.HalLink;
import com.garmin.garminpay.model.response.HealthResponse;
import com.garmin.garminpay.model.response.RegisterCardResponse;
import com.garmin.garminpay.model.response.RootResponse;
import com.garmin.garminpay.proxy.GarminPayProxy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.SneakyThrows;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class GarminPayProxyTest {
    private static final String testingUrl = "http://localhost";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Header[] testingHeaders = {
        new BasicHeader("CF-RAY", "testing-cf-ray")
    };
    private final Map<String, HalLink> links = new HashMap<>();
    private static final String TESTING_DEEP_LINK = "https://connect.garmin.com/payment/directpush?pushToken=test";
    private RefreshableOauthClient refreshableOauthClient;
    private GarminPayProxy garminPayProxy;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        refreshableOauthClient = mock(RefreshableOauthClient.class);

        links.put("self", HalLink.builder().href(testingUrl).build());
        links.put("health", HalLink.builder().href(testingUrl + "/health").build());
        links.put("encryptionKeys", HalLink.builder().href(testingUrl + "/config/encryptionKeys").build());
        links.put("paymentCards", HalLink.builder().href(testingUrl + "/paymentCards").build());

        RootResponse successResponse = RootResponse.builder()
            .links(new HashMap<>(links))
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .content(objectMapper.writeValueAsString(successResponse))
            .headers(testingHeaders)
            .build();

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        garminPayProxy = new GarminPayProxy(refreshableOauthClient, testingUrl);
        garminPayProxy.refreshRootLinks();
    }

    @Test
    void testGetRootEndpointSuccess() throws JsonProcessingException {
        RootResponse successResponse = RootResponse.builder()
            .links(new HashMap<>(links))
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .content(objectMapper.writeValueAsString(successResponse))
            .headers(testingHeaders)
            .build();

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        RootResponse rootResponse = garminPayProxy.getRootEndpoint();

        assertEquals(successResponse.getLinks(), rootResponse.getLinks());
    }

    @Test
    void testGetRootEndpointFailure() throws JsonProcessingException {
        ErrorResponse failureResponse = ErrorResponse.builder()
            .path("/")
            .status(HttpStatus.SC_NOT_FOUND)
            .message("Not Found")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_NOT_FOUND)
            .content(objectMapper.writeValueAsString(failureResponse))
            .headers(testingHeaders)
            .build();

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getRootEndpoint());

        assertEquals(HttpStatus.SC_NOT_FOUND, exception.getStatus());
    }

    @Test
    void testGetRootEndpointBadGateway() throws JsonProcessingException {
        ErrorResponse failureResponse = ErrorResponse.builder()
            .path("/")
            .status(HttpStatus.SC_BAD_GATEWAY)
            .message("Bad Gateway")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_BAD_GATEWAY)
            .content(objectMapper.writeValueAsString(failureResponse))
            .headers(testingHeaders)
            .build();

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getRootEndpoint());

        assertEquals(HttpStatus.SC_BAD_GATEWAY, exception.getStatus());
    }

    @Test
    void testGetHealthStatusSuccess() throws JsonProcessingException {
        HealthResponse successResponse = HealthResponse.builder()
            .healthStatus("OK")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .content(objectMapper.writeValueAsString(successResponse))
            .headers(testingHeaders)
            .build();

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        HealthResponse healthResponse = garminPayProxy.getHealthStatus();

        assertEquals("OK", healthResponse.getHealthStatus());
    }

    @Test
    void testGetHealthStatusFailure() throws JsonProcessingException {
        ErrorResponse failureResponse = ErrorResponse.builder()
            .path("/")
            .status(HttpStatus.SC_NOT_FOUND)
            .message("Not Found")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_NOT_FOUND)
            .content(objectMapper.writeValueAsString(failureResponse))
            .headers(testingHeaders)
            .build();

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getHealthStatus());

        assertEquals(HttpStatus.SC_NOT_FOUND, exception.getStatus());
    }

    @Test
    void testGetHealthStatusBadGateway() throws JsonProcessingException {
        ErrorResponse failureResponse = ErrorResponse.builder()
            .path("/health")
            .status(HttpStatus.SC_BAD_GATEWAY)
            .message("Bad Gateway")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_BAD_GATEWAY)
            .content(objectMapper.writeValueAsString(failureResponse))
            .headers(testingHeaders)
            .build();

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getHealthStatus());

        assertEquals(HttpStatus.SC_BAD_GATEWAY, exception.getStatus());
    }

    @Test
    void testPostExchangeKeysSuccess() throws JsonProcessingException {
        String mockServerKeyId = UUID.randomUUID().toString();
        ExchangeKeysResponse successResponse = ExchangeKeysResponse.builder()
            .serverPublicKey(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY)
            .keyId(mockServerKeyId)
            .active(true)
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .content(objectMapper.writeValueAsString(successResponse))
            .headers(testingHeaders)
            .build();

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        ExchangeKeysResponse exchangeKeysResponse = garminPayProxy.exchangeKeys(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);

        assertEquals(exchangeKeysResponse.getKeyId(), successResponse.getKeyId());
        assertEquals(exchangeKeysResponse.getServerPublicKey(), successResponse.getServerPublicKey());
    }

    @Test
    void testPostExchangeKeysFailure() throws JsonProcessingException {
        ErrorResponse failureResponse = ErrorResponse.builder()
            .path("/config/encryptionKeys")
            .status(HttpStatus.SC_BAD_REQUEST)
            .message("Bad Request")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_BAD_REQUEST)
            .content(objectMapper.writeValueAsString(failureResponse))
            .headers(testingHeaders)
            .path("/config/encryptionKeys")
            .build();

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.exchangeKeys(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY));

        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatus());
    }

    @SneakyThrows
    @Test
    void testPostRegisterCardSuccess() throws JsonProcessingException {
        RegisterCardResponse successResponse = RegisterCardResponse.builder()
            .deepLinkUrl(TESTING_DEEP_LINK)
            .pushId("test")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .content(objectMapper.writeValueAsString(successResponse))
            .headers(testingHeaders)
            .build();

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        RegisterCardResponse registerCardResponse = garminPayProxy.registerCard("mockEncryptedCardData");

        assertEquals(TESTING_DEEP_LINK, registerCardResponse.getDeepLinkUrl());
        assertEquals("test", registerCardResponse.getPushId());
    }

    @Test
    void testPostRegisterCardFailure() throws JsonProcessingException {
        ErrorResponse failureResponse = ErrorResponse.builder()
            .path("/paymentCards")
            .status(HttpStatus.SC_BAD_REQUEST)
            .message("Bad Request")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_BAD_REQUEST)
            .content(objectMapper.writeValueAsString(failureResponse))
            .headers(testingHeaders)
            .build();

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.registerCard("mockEncryptedCardData"));

        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatus());
    }
}
