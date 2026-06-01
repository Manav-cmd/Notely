package com.notely.service;

import com.notely.dto.RegisterRequest;
import com.notely.dto.UserResponse;
import com.notely.entity.Role;
import com.notely.entity.User;
import com.notely.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@email.com");
        request.setUsername("testuser");
        request.setPassword("password");

        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
        
        User mockSaved = User.builder()
                .email("test@email.com")
                .username("testuser")
                .password("hashedPassword")
                .role(Role.ROLE_USER)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(mockSaved);

        UserResponse result = userService.registerUser(request);

        assertNotNull(result);
        assertEquals("test@email.com", result.getEmail());
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
