package com.authService.services;


import com.authService.AOP.SendNotification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
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
        * Generates a 6-digit OTP, stores it in Redis with a TTL, and returns an event for AOP to handle.
        */
    public OtpEvent generateAndSendOtp(String email) {
        log.info("Generating OTP for email: {}", email);
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Efficient Storage: Key = "otp:email", Value = code, TTL = 5 mins
        redisTemplate.opsForValue().set("otp:" + email, otp, OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        log.info("OTP stored in Redis for email: {} with TTL: {} minutes", email, OTP_EXPIRATION_MINUTES);

        // Return event for AOP to handle the Kafka message
        return new OtpEvent(email, otp);
    }

    /**
     * Validates the OTP.
     * @return true if valid, false otherwise
     */
    public boolean validateOtp(String email, String otpInput) {
        log.info("Validating OTP for email: {}", email);
        String key = "otp:" + email;
        String cachedOtp = redisTemplate.opsForValue().get(key);

        if (cachedOtp != null && cachedOtp.equals(otpInput)) {
            log.info("OTP validation successful for email: {}", email);
            redisTemplate.delete(key); // Cleanup immediately after use
            return true;
        }
        log.warn("OTP validation failed for email: {}", email);
        return false;
    }
}