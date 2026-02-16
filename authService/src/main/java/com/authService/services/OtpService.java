package com.authService.services;


import com.authService.AOP.SendNotification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private static final long OTP_EXPIRATION_MINUTES = 5;

    // Helper class to pass data to AOP
    @Data
    @AllArgsConstructor
    public static class OtpEvent {
        private String email;
        private String otpCode;
    }

    /**
     * Generates OTP, saves to Redis, and triggers AOP for Email Notification.
     * @param email - The user's email
     * @return OtpEvent - intercepted by AOP
     */
    @SendNotification(topic = "notification-topic", eventType = "OTP_GENERATED")
    public OtpEvent generateAndSendOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Efficient Storage: Key = "otp:email", Value = code, TTL = 5 mins
        redisTemplate.opsForValue().set("otp:" + email, otp, OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        // Return event for AOP to handle the Kafka message
        return new OtpEvent(email, otp);
    }

    /**
     * Validates the OTP.
     * @return true if valid, false otherwise
     */
    public boolean validateOtp(String email, String otpInput) {
        String key = "otp:" + email;
        String cachedOtp = redisTemplate.opsForValue().get(key);

        if (cachedOtp != null && cachedOtp.equals(otpInput)) {
            redisTemplate.delete(key); // Cleanup immediately after use
            return true;
        }
        return false;
    }
}