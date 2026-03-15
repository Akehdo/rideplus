package akendo.identityservice.controller;

import akendo.identityservice.dto.AuthResponse;
import akendo.identityservice.dto.LoginRequest;
import akendo.identityservice.dto.RegisterRequest;
import akendo.identityservice.dto.LogoutRequest;
import akendo.identityservice.dto.LogoutResponse;
import akendo.identityservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(
                request.getEmail(),
                request.getPassword()
        );
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(
                request.getEmail(),
                request.getPassword()
        );
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestParam String refreshToken) {
        return authService.refresh(refreshToken);
    }

    @PostMapping("/logout")
    public LogoutResponse logout(@RequestBody LogoutRequest request) {
        return authService.logout(request.getRefreshToken());
    }
}