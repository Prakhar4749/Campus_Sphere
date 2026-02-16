package com.authService.services;


import com.authService.AOP.SendNotification;
import com.authService.DTO.AuthResponse;
import com.authService.DTO.LoginRequest;
import com.authService.DTO.SignupRequest;
import com.authService.entities.User;
import com.authService.enums.AccountStatus;
import com.authService.jwtSecurity.JwtService;
import com.authService.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    // 1. SIGNUP: Enforces OTP Validation
    @Transactional
    @SendNotification(topic = "notification-topic", eventType = "USER_REGISTERED")
    public User registerUser(SignupRequest request) {
        // Step A: Check if User already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Step B: Verify OTP using the separate service
        boolean isOtpValid = otpService.validateOtp(request.getEmail(), request.getOtp());
        if (!isOtpValid) {
            throw new RuntimeException("Invalid or Expired OTP. Please request a new one.");
        }

        // Step C: Proceed to Save
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enrollmentNo(request.getEnrollmentNo())
                .collegeId(request.getCollegeId())
                .departmentId(request.getDepartmentId())
                .role(request.getRole())
                .status(AccountStatus.PENDING)
                .isEmailVerified(true) // Now we can set this to true
                .build();

        return userRepository.save(user);
    }

    // 2. UPDATE STATUS: Called by OpenFeign (Sync)
    // Triggers: Profile Creation (Kafka) + User Notification (Kafka)
    @Transactional
    @SendNotification(topic = "user-events-topic", eventType = "STATUS_CHANGED")
    public User updateUserStatus(String email, AccountStatus newStatus) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(newStatus);
        return userRepository.save(user);
    }

    // 3. LOGIN
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.isPasswordChangeRequired()) {
            throw new RuntimeException("PASSWORD_RESET_REQUIRED");
            // Frontend catches this -> Redirects to /change-password screen
        }
        if (user.getStatus() != AccountStatus.APPROVED) {
            throw new RuntimeException("Account is " + user.getStatus());
        }

        return new AuthResponse(jwtService.generateToken(user), user.getStatus().toString());
    }

    // 1. FORGOT PASSWORD: Check User & Send OTP
    @SendNotification(topic = "notification-topic", eventType = "OTP_GENERATED")
    public OtpService.OtpEvent forgotPassword(String email) {
        // A. Validate Email exists
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("User not found with this email.");
        }

        // B. Generate OTP (Reuse existing service)
        return otpService.generateAndSendOtp(email);
    }

    // 2. RESET PASSWORD: Validate OTP & Update DB
    @Transactional
    @SendNotification(topic = "notification-topic", eventType = "PASSWORD_CHANGED")
    public String resetPassword(String email, String otp, String newPassword) {
        // A. Validate OTP (Reuse existing service)
        boolean isOtpValid = otpService.validateOtp(email, otp);
        if (!isOtpValid) {
            throw new RuntimeException("Invalid or Expired OTP.");
        }

        // B. Fetch User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        // C. Update Password (Encrypt it!)
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password reset successfully. You can now login.";
    }

    // 1. INTERNAL: Create Admin User (Called by Admin Service via Feign)
    @Transactional
    @SendNotification(topic = "notification-topic", eventType = "ADMIN_USER_CREATED")
    public User createAdminUser(SignupRequest request) {
        // Validation
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                // Password is provided by Admin Service (Randomly generated there)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole()) // COLLEGE_ADMIN or DEPT_ADMIN
                .status(AccountStatus.APPROVED) // Auto-approved
                .isEmailVerified(true)
                .isPasswordChangeRequired(true) // FORCE RESET
                .build();

        return userRepository.save(user);
    }

    // 3. CHANGE PASSWORD (Removes the Flag)
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        // Validate Old Password
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, oldPassword));

        User user = userRepository.findByEmail(email).orElseThrow();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangeRequired(false);
        userRepository.save(user);
    }
}