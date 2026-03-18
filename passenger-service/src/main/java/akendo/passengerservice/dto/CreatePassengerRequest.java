package akendo.passengerservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePassengerRequest(
        @NotBlank(message = "firstName is required")
        String firstName,

        @NotBlank(message = "lastName is required")
        String lastName
) {}