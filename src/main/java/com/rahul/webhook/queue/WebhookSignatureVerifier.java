package com.rahul.webhook.queue;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class WebhookSignatureVerifier {

    public boolean verify(String payload, String secret, String receivedSignature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

            mac.init(key);

            byte[] expected = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            String expectedSignature = HexFormat.of().formatHex(expected);

            return MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), receivedSignature.getBytes(StandardCharsets.UTF_8));

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Signature verification failed", e);
        }
    }
}
