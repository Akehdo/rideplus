package akendo.identityservice.dto;

public record LogoutRequest(
        String refreshToken
) {
}