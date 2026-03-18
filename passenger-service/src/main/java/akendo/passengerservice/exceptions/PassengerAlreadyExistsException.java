package akendo.passengerservice.exceptions;

public class PassengerAlreadyExistsException extends RuntimeException {
    public PassengerAlreadyExistsException(String userId) {
        super("Passenger with id: " + userId + " already exists");
    }
}
