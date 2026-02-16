package com.authService.controllers;

import com.authService.DTO.*;
import com.authService.entities.User;
import com.authService.enums.AccountStatus;
import com.authService.services.AuthService;
import com.authService.services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    // 1. Generate OTP
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestParam String email) {
        otpService.generateAndSendOtp(email);
        return ResponseEntity.ok("OTP sent successfully to " + email);
    }

    // 2. Signup (Requires OTP in request body)
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok("User verified and registered. Waiting for Department Approval.");
    }

    // Public: Login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // 3. Forgot Password (Step 1)
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok("OTP sent to your email for password reset.");
    }

    // 4. Reset Password (Step 2)
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        String result = authService.resetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword()
        );
        return ResponseEntity.ok(result);
    }
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request.getEmail(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok("Password changed successfully.");
    }

    // INTERNAL: Called by Approval Service via OpenFeign
    @PutMapping("/internal/update-status")
    public ResponseEntity<String> updateStatus(@RequestParam String email, @RequestParam AccountStatus status) {
        authService.updateUserStatus(email, status);
        return ResponseEntity.ok("Status updated to " + status);
    }
    // Internal Endpoint: Protected by Gateway Secret
    @PostMapping("/internal/create-admin")
    public ResponseEntity<Long> createAdmin(@RequestBody SignupRequest request) {
        User user = authService.createAdminUser(request);
        return ResponseEntity.ok(user.getId()); // Return ID to Admin Service
    }
}