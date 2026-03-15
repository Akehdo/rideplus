package akendo.identityservice.service;

import akendo.identityservice.security.JwtProperties;
import akendo.identityservice.domain.User;
import akendo.identityservice.dto.AuthResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey(String key) {
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    public AuthResponse generateTokens(User user){
        Instant now = Instant.now();

        String accessToken = Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtProperties.getAccessExpiration())))
                .signWith(getSigningKey(jwtProperties.getAccessSecret()))
                .compact();

        String refreshToken = Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtProperties.getRefreshExpiration())))
                .signWith(getSigningKey(jwtProperties.getRefreshSecret()))
                .compact();

        return new AuthResponse(accessToken, refreshToken);
    }



    public void validateRefreshToken(String token) {
        Jwts.parser()
                .verifyWith(getSigningKey(jwtProperties.getRefreshSecret()))
                .build()
                .parseSignedClaims(token);
    }


}
