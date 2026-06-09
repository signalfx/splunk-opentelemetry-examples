package com.splunk.workshop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class OrderController {

    private static final Logger logger = LogManager.getLogger(OrderController.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${server.port:8080}")
    private int serverPort;

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "healthy");
    }

    @GetMapping("/order")
    @SuppressWarnings("unchecked")
    public Map<String, Object> getOrder() {
        logger.info("Processing GET /order request");
        simulateWork(20, 80);

        Map<String, Object> inventory = restTemplate.getForObject(
                "http://localhost:" + serverPort + "/inventory/check", Map.class);

        Map<String, Object> payment = restTemplate.getForObject(
                "http://localhost:" + serverPort + "/payment/process", Map.class);

        return Map.of(
            "orderId", UUID.randomUUID().toString(),
            "status", "created",
            "payment", payment != null ? payment.get("result") : "unknown",
            "inventory", inventory != null ? inventory.get("available") : 0
        );
    }

    @PostMapping("/order")
    @SuppressWarnings("unchecked")
    public Map<String, Object> createOrder(@RequestBody(required = false) Map<String, Object> body) {
        logger.info("Processing POST /order request");
        simulateWork(30, 120);

        Map<String, Object> payment = restTemplate.getForObject(
                "http://localhost:" + serverPort + "/payment/process", Map.class);

        return Map.of(
            "orderId", UUID.randomUUID().toString(),
            "status", "confirmed",
            "payment", payment != null ? payment.get("result") : "unknown",
            "items", body != null ? body.getOrDefault("items", 1) : 1
        );
    }

    private void simulateWork(int minMs, int maxMs) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(minMs, maxMs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
