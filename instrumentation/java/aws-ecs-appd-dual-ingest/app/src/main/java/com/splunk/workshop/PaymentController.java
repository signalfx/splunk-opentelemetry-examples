package com.splunk.workshop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private static final Logger logger = LogManager.getLogger(PaymentController.class);

    @GetMapping("/process")
    public Map<String, Object> processPayment() {
        simulateWork(40, 150);
        String result = ThreadLocalRandom.current().nextInt(100) < 5 ? "declined" : "approved";
        logger.info("Payment processed with result: {}", result);
        return Map.of(
            "result", result,
            "processor", "stripe",
            "latencyMs", ThreadLocalRandom.current().nextInt(40, 150)
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
