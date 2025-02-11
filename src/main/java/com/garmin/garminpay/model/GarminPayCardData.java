/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garmin.garminpay.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class GarminPayCardData {
    /**
     * The primary account number (PAN) of the card.
     */
    @NonNull
    String pan;

    /**
     * The Card Verification Value (CVV) of the card.
     */
    String cvv;

    /**
     * The expiration month of the card.
     */
    Integer expMonth;

    /**
     * The expiration year of the card.
     */
    Integer expYear;

    /**
     * The name of the cardholder.
     */
    String name;

    /**
     * The billing address associated with the card.
     */
    Address address;
}
