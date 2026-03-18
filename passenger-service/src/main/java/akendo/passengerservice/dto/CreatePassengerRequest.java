package akendo.passengerservice.dto;

public record CreatePassengerRequest(
        String firstName,
        String lastName
) {}