package com.authService.controllers;

import com.authService.DTO.*;
import com.authService.entities.User;
import com.authService.enums.AccountStatus;
import com.authService.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    // 1. Generate OTP
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@RequestParam String email) {
        log.info("Request received: Send OTP for verification to {}", email);
        authService.verifyEmail(email);
        return ResponseEntity.ok(ApiResponse.success(null, "OTP sent successfully to " + email));
    }

    // 2. Signup (Requires OTP in request body)
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Request received: User signup for {}", request.getEmail());
        authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "User verified and registered. Waiting for Department Approval."));
    }

    // Public: Login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Request received: Login for {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful."));
    }

    // 3. Forgot Password (Step 1)
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        log.info("Request received: Forgot password OTP for {}", email);
        authService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success(null, "OTP sent to your email for password reset."));
    }

    // 4. Reset Password (Step 2)
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Request received: Reset password for {}", request.getEmail());
        String result = authService.resetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword()
        );
        return ResponseEntity.ok(ApiResponse.success(null, result));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Request received: Change password for {}", request.getEmail());
        authService.changePassword(request.getEmail(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully."));
    }

    // INTERNAL: Called by Approval Service via OpenFeign
    @PutMapping("/internal/update-status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@RequestParam String email, @RequestParam AccountStatus status) {
        log.info("Internal request: Update status for {} to {}", email, status);
        authService.updateUserStatus(email, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Status updated to " + status));
    }

    // Internal Endpoint: Protected by Gateway Secret
    @PostMapping("/internal/create-admin")
    public ResponseEntity<ApiResponse<Long>> createAdmin(@Valid @RequestBody SignupRequest request) {
        log.info("Internal request: Create admin user for email: {}", request.getEmail());
        User user = authService.createAdminUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user.getId(), "Admin user created successfully."));
    }
}