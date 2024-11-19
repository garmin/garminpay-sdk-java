/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garminpay.exception;

import com.garminpay.model.response.ErrorResponse;

public final class GarminPayMaintenanceException extends GarminPayApiException {

    /**
     * Constructs a new GarminPayMaintenanceException with the specified detail message.
     *
     * @param message       the detail message.
     * @param errorResponse error response returned from the API.
     */
    public GarminPayMaintenanceException(String message, ErrorResponse errorResponse) {
        super(message, errorResponse);
    }
}
