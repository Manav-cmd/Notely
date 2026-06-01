package com.notely.service;

import com.notely.dto.JwtResponse;
import com.notely.dto.LoginRequest;
import com.notely.dto.RegisterRequest;
import com.notely.dto.UserResponse;
import com.notely.entity.User;
import java.util.UUID;

public interface UserService {
    UserResponse registerUser(RegisterRequest registerRequest);
    JwtResponse authenticateUser(LoginRequest loginRequest);
    UserResponse getUserProfile(UUID userId);
    UserResponse updateProfile(UUID userId, String username);
    void resetPassword(String email, String newPassword);
    User getCurrentUserEntity();
}
