package com.babgo.repository.payment.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class ClientResponse {

    @Getter
    @RequiredArgsConstructor
    public static class create {
        private final int code;
        private final String url;
    }
}
