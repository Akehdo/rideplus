package akendo.identityservice.dto;

public record LoginRequest(
        String email,
        String password)
{
}