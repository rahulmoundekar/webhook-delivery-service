package com.rahul.webhook.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestWebhookController {

    @PostMapping("/test-webhook")
    public ResponseEntity<String> receive(@RequestHeader("X-Webhook-Signature") String signature, @RequestHeader("X-Webhook-Event") String event, @RequestBody String payload) {

        System.out.println("Event: " + event);
        System.out.println("Signature: " + signature);
        System.out.println("Payload: " + payload);

        return ResponseEntity.ok("received");
    }
}