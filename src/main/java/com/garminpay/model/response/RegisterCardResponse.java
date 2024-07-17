package com.garminpay.model.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder
public class RegisterCardResponse {
    String deepLinkUrlIos;
    String deepLinkUrlAndroid;
}
