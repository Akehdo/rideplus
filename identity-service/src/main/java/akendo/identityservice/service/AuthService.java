package akendo.identityservice.service;

import akendo.identityservice.security.JwtProperties;
import akendo.identityservice.domain.RefreshToken;
import akendo.identityservice.domain.User;
import akendo.identityservice.dto.AuthResponse;
import akendo.identityservice.dto.LogoutResponse;
import akendo.identityservice.exception.InvalidCredentialsException;
import akendo.identityservice.exception.UserAlreadyExistsException;
import akendo.identityservice.repository.RefreshTokenRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final JwtService jwtService;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final MeterRegistry meterRegistry;

    public AuthResponse register(String email, String password) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String result = "error";

        try {
            if (userService.existsByEmail(email)) {
                result = "duplicate";
                throw new UserAlreadyExistsException(email);
            }

            String hashedPassword = passwordEncoder.encode(password);
            User user = userService.create(email, hashedPassword);
            AuthResponse tokens = jwtService.generateTokens(user);

            RefreshToken refreshToken = RefreshToken.create(
                    user,
                    tokens.refreshToken(),
                    Instant.now().plusSeconds(jwtProperties.getRefreshExpiration())
            );

            refreshTokenRepository.save(refreshToken);

            result = "success";
            return tokens;
        } catch (UserAlreadyExistsException ex) {
            throw ex;
        } finally {
            meterRegistry.counter("rideplus.auth.register.total", "result", result).increment();
            sample.stop(meterRegistry.timer("rideplus.auth.register.duration", "result", result));
        }
    }


    public AuthResponse login(String email, String password){
        User user = userService.findByEmail(email);
        if(user == null || !passwordEncoder.matches(password,user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        AuthResponse tokens = jwtService.generateTokens(user);

        RefreshToken refreshToken = RefreshToken.create(
                user,
                tokens.refreshToken(),
                Instant.now().plusSeconds(jwtProperties.getRefreshExpiration())
        );

        refreshTokenRepository.save(refreshToken);

        return tokens;
    }

    public LogoutResponse logout(String refreshToken) {
        RefreshToken storedRefreshToken = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(InvalidCredentialsException::new);

        if(!storedRefreshToken.isActive()) {
            throw new InvalidCredentialsException();
        }

        storedRefreshToken.revoke();

        return new LogoutResponse("Logged out successfully");
    }

    public AuthResponse refresh(String refreshToken) {
        jwtService.validateRefreshToken(refreshToken);

        RefreshToken storedToken = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(InvalidCredentialsException::new);

        if(!storedToken.isActive()) {
            throw new InvalidCredentialsException();
        }

        User user = storedToken.getUser();

        storedToken.revoke();

        AuthResponse newTokens = jwtService.generateTokens(user);

        RefreshToken newRefresh = RefreshToken.create(
                user,
                newTokens.refreshToken(),
                Instant.now().plusSeconds(jwtProperties.getRefreshExpiration())
        );

        refreshTokenRepository.save(newRefresh);

        return newTokens;
    }
}
