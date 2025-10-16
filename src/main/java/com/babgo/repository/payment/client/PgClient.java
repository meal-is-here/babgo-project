package com.babgo.repository.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient( name = "pg-simulator", url  = "http://localhost:8080" )
public interface PgClient {

    @PostMapping(value = "/pg-sim/api/v2/payments", consumes = "application/json")
    ClientResponse.create create(@RequestBody ClientRequest.create body);

    @PostMapping(value = "/pg-sim/v1/payments/confirm", consumes = "application/json")
    void confirm(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody ClientRequest.confirm body
    );


}
