package com.simplypos.modules.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simplypos.config.StoreContext;
import com.simplypos.core.security.JwtUtil;
import com.simplypos.entities.User;
import com.simplypos.modules.auth.dto.LoginRequest;
import com.simplypos.modules.auth.dto.LoginResponse;
import com.simplypos.modules.auth.dto.UserDto;
import com.simplypos.modules.auth.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final StoreContext storeContext;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Record last login
        user.setLastLoginAt(java.time.LocalDateTime.now());
        user.setLastLoginIp(httpRequest.getRemoteAddr());
        userRepository.save(user);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole());
        if (user.getStore() != null) {
            response.setStoreId(user.getStore().getId());
            response.setStoreCode(user.getStore().getCode());
            response.setStoreName(user.getStore().getName());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        if (user.getStore() != null) {
            dto.setStoreId(user.getStore().getId());
            dto.setStoreName(user.getStore().getName());
        }

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Dengan JWT stateless, logout dilakukan di sisi client (hapus token)
        // Server bisa implementasi blacklist jika diperlukan
        return ResponseEntity.ok("Logged out successfully");
    }
}