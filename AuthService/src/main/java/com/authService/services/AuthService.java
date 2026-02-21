package com.authService.services;

import com.authService.AOP.SendNotification;
import com.authService.DTO.AuthResponse;
import com.authService.DTO.LoginRequest;
import com.authService.DTO.SignupRequest;
import com.authService.entities.User;
import com.authService.enums.AccountStatus;
import com.authService.exceptions.*;
import com.authService.jwtSecurity.JwtService;
import com.authService.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    @Transactional
    @SendNotification(topic = "notification.user", eventType = "USER_REGISTERED")
    public User registerUser(SignupRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email {} already exists.", request.getEmail());
            throw new DuplicateResourceException("Email already exists.");
        }

        boolean isOtpValid = otpService.validateOtp(request.getEmail(), request.getOtp());
        if (!isOtpValid) {
            log.warn("Registration failed: Invalid OTP for email {}.", request.getEmail());
            throw new InvalidOtpException("Invalid or Expired OTP. Please request a new one.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enrollmentNo(request.getEnrollmentNo())
                .collegeId(request.getCollegeId())
                .departmentId(request.getDepartmentId())
                .role(request.getRole())
                .status(AccountStatus.PENDING)
                .isEmailVerified(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} with ID: {}", savedUser.getEmail(), savedUser.getId());
        return savedUser;
    }

    @Transactional
    @SendNotification(topic = "notification.system", eventType = "STATUS_CHANGED")
    public User updateUserStatus(String email, AccountStatus newStatus) {
        log.info("Updating status for user: {} to {}", email, newStatus);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Status update failed: User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });

        user.setStatus(newStatus);
        User updatedUser = userRepository.save(user);
        log.info("Status updated successfully for user: {}", email);
        return updatedUser;
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Login failed: Invalid credentials for email: {}", request.getEmail());
            throw new InvalidOtpException("Invalid email or password."); 
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Login failed: User record disappeared for email: {}", request.getEmail());
                    return new ResourceNotFoundException("User not found.");
                });

        if (user.isPasswordChangeRequired()) {
            log.info("Login required password reset for user: {}", request.getEmail());
            throw new PasswordResetRequiredException("You must change your password before logging in.");
        }
        if (user.getStatus() != AccountStatus.APPROVED) {
            log.warn("Login denied: User {} has status {}", request.getEmail(), user.getStatus());
            throw new AccountStatusException("Your account status is currently: " + user.getStatus());
        }

        log.info("User logged in successfully: {}", request.getEmail());
        return new AuthResponse(jwtService.generateToken(user), user.getStatus().toString());
    }

    @SendNotification(topic = "notification.otp", eventType = "OTP_PASSWORD_RESET")
    public OtpService.OtpEvent forgotPassword(String email) {
        log.info("Password reset requested for email: {}", email);
        if (!userRepository.existsByEmail(email)) {
            log.warn("Forgot password failed: Email {} not found.", email);
            throw new ResourceNotFoundException("User not found with this email.");
        }
        return otpService.generateAndSendOtp(email);
    }

    @SendNotification(topic = "notification.otp", eventType = "OTP_EMAIL_VERIFICATION")
    public OtpService.OtpEvent verifyEmail(String email) {
        log.info("Email verification requested for: {}", email);
        return otpService.generateAndSendOtp(email);
    }

    @Transactional
    @SendNotification(topic = "notification.user", eventType = "PASSWORD_CHANGED")
    public String resetPassword(String email, String otp, String newPassword) {
        log.info("Attempting to reset password for email: {}", email);
        boolean isOtpValid = otpService.validateOtp(email, otp);
        if (!isOtpValid) {
            log.warn("Password reset failed: Invalid OTP for email: {}", email);
            throw new InvalidOtpException("Invalid or Expired OTP.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Password reset failed: User not found for email: {}", email);
                    return new ResourceNotFoundException("User not found.");
                });

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset successfully for email: {}", email);

        return "Password reset successfully. You can now login.";
    }

    @Transactional
    @SendNotification(topic = "notification.user", eventType = "ADMIN_USER_CREATED")
    public User createAdminUser(SignupRequest request) {
        log.info("Creating admin user with email: {} and role: {}", request.getEmail(), request.getRole());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Admin creation failed: Email {} already exists.", request.getEmail());
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(AccountStatus.APPROVED)
                .isEmailVerified(true)
                .isPasswordChangeRequired(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Admin user created successfully: {}", savedUser.getEmail());
        return savedUser;
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        log.info("Attempting to change password for email: {}", email);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, oldPassword));
        } catch (BadCredentialsException e) {
            log.warn("Change password failed: Incorrect old password for email: {}", email);
            throw new InvalidOtpException("Incorrect old password.");
        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("Change password failed: User not found for email: {}", email);
            return new ResourceNotFoundException("User not found.");
        });
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangeRequired(false);
        userRepository.save(user);
        log.info("Password changed successfully for email: {}", email);
    }
}