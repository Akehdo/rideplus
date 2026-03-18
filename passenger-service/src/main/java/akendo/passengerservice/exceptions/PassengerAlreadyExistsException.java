package akendo.passengerservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PassengerAlreadyExistsException extends RuntimeException {
    public PassengerAlreadyExistsException(String userId) {
        super("Passenger with id: " + userId + " already exists");
    }
}
