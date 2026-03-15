package akendo.identityservice.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
