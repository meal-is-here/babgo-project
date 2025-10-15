package com.babgo.controller.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@RestController
@RequestMapping("/pg-sim")
public class PgSimulatorController {

    private final Map<String, String> idemCache = new ConcurrentHashMap<>();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final RestTemplate rest = new RestTemplate();
    @Value("${pg-sim.auto:}")   // "", "approve", "decline"
    private String defaultAuto;
    // 키 이름을 한 군데로 고정
    // - orderId, successUrl, failUrl, amount, resultCallback(webhook), auto(optional: approve|decline)
    record Session(String successUrl, String failUrl, Integer amount, String webhookUrl) {}

    @PostMapping("/api/v2/payments")
    public Map<String,Object> create(@RequestBody Map<String,Object> body) {
        log.info("Pg create");
        String orderId     = (String)  body.get("orderId");
        String successUrl  = (String)  body.get("successUrl");
        String failUrl     = (String)  body.get("failUrl");
        Integer amount     = (Integer) body.get("amount");
        String webhookUrl  = (String)  body.get("resultCallback"); // optional
        String auto       = (String)  body.getOrDefault("auto", defaultAuto);  // "approve" | "decline" | null

        // 필수값 체크
        if (orderId == null || successUrl == null || failUrl == null || amount == null) {
            return Map.of("code", 1, "message", "missing required fields");
        }

        sessions.put(orderId, new Session(successUrl, failUrl, amount, webhookUrl));

        // auto 모드면 사용자 클릭 없이 성공/실패 콜백을 서버로 바로 쏴줌
        if ("approve".equalsIgnoreCase(auto) || "decline".equalsIgnoreCase(auto)) {
            CompletableFuture.runAsync(() -> {
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                Session s = sessions.get(orderId);
                if (s == null) return;

                if ("approve".equalsIgnoreCase(auto)) {
                    String paymentKey = UUID.randomUUID().toString();
                    String redirect = UriComponentsBuilder.fromUriString(s.successUrl())
                            .queryParam("paymentKey", paymentKey)
                            .queryParam("orderId", orderId)
                            .queryParam("amount", s.amount())
                            .build().toUriString();

                    try { rest.getForEntity(redirect, String.class); } catch (Exception ignore) {}
                } else {
                    String redirect = UriComponentsBuilder.fromUriString(s.failUrl())
                            .queryParam("code", "SIM_ERR")
                            .queryParam("message", "Declined by simulator")
                            .queryParam("orderId", orderId)
                            .build().toUriString();
                    try { rest.getForEntity(redirect, String.class); } catch (Exception ignore) {}
                }
            });
        }

        return Map.of(
                "code", 0,
                "checkoutPage", "http://localhost:8080/pg-sim/checkout?o=" + orderId
        );
    }


    @PostMapping("/approve")
    public ResponseEntity<Void> approve(@RequestParam String orderId) {
        Session s = sessions.get(orderId);
        if (s == null) return ResponseEntity.notFound().build();
        String paymentKey = UUID.randomUUID().toString();
        String redirect = UriComponentsBuilder.fromUriString(s.successUrl())
                .queryParam("paymentKey", paymentKey)
                .queryParam("orderId", orderId)
                .queryParam("amount", s.amount())
                .build().toUriString();
        return ResponseEntity.status(302).header("Location", redirect).build();
    }

    @PostMapping("/decline")
    public ResponseEntity<Void> decline(@RequestParam String orderId) {
        Session s = sessions.get(orderId);
        if (s == null) return ResponseEntity.notFound().build();
        String redirect = UriComponentsBuilder.fromUriString(s.failUrl())
                .queryParam("code", "SIM_ERR")
                .queryParam("message", "Declined by simulator")
                .queryParam("orderId", orderId)
                .build().toUriString();
        return ResponseEntity.status(302).header("Location", redirect).build();
    }

    @PostMapping("/v1/payments/confirm")
    public Map<String,Object> confirm(
            @RequestHeader("Idempotency-Key") String idem,
            @RequestBody Map<String,Object> body
    ) {
        String orderId    = (String) body.get("orderId");
        Integer amount    = (Integer) body.get("amount");
        String paymentKey = (String) body.get("paymentKey");

        String txKey = idemCache.computeIfAbsent(idem, k -> "SIM-TX-" + orderId);
        boolean approved = (amount != null && amount % 2 == 0);

        Session s = sessions.get(orderId);
        if (s != null && s.webhookUrl() != null) {
            CompletableFuture.runAsync(() -> {
                try { Thread.sleep(600); } catch (InterruptedException ignored) {}
                Map<String,Object> webhook = Map.of(
                        "event", "PAYMENT_STATUS_CHANGED",
                        "orderId", orderId,
                        "paymentKey", paymentKey,
                        "transactionKey", txKey,
                        "approved", approved,
                        "status", approved ? "APPROVED" : "DECLINED",
                        "providerCode", approved ? "0" : "SIM_ERR"
                );
                try { rest.postForEntity(s.webhookUrl(), webhook, Void.class); } catch (Exception ignore) {}
            });
        }

        return Map.of(
                "status", approved ? "APPROVED" : "DECLINED",
                "transactionKey", txKey,
                "code", approved ? "0" : "SIM_ERR"
        );
    }
}
