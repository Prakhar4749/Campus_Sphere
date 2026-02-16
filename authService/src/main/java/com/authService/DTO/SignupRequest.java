package com.authService.DTO;
import com.authService.enums.Role;
import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String password;
    private String otp; // Required for signup
    private String enrollmentNo;
    private Long collegeId;
    private Long departmentId;
    private Role role;
}