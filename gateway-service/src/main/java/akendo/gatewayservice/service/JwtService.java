package akendo.gatewayservice.service;

import akendo.gatewayservice.security.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public Claims extractPayloadFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey(jwtProperties.getAccessSecret()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUserIdFromToken(String token) {
        return extractPayloadFromToken(token).getSubject();
    }

    private SecretKey getSigningKey(String key){
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }
}
