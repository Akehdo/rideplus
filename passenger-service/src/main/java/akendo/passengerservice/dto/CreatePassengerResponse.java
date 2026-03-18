package akendo.passengerservice.dto;

import java.time.Instant;
import java.util.UUID;

public record CreatePassengerResponse(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        Double rating,
        Instant createdAt
) {}