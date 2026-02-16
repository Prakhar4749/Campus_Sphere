package com.authService.DTO;
import com.authService.enums.Role;
import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
