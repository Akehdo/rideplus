package akendo.passengerservice.service;

import akendo.passengerservice.domain.Passenger;
import akendo.passengerservice.exceptions.PassengerAlreadyExistsException;
import akendo.passengerservice.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor()
public class PassengerService {
    private final PassengerRepository passengerRepository;

    public Passenger create(UUID userId, String firstName, String lastName) {
        if(passengerRepository.existsByUserId(userId)){
            throw new PassengerAlreadyExistsException(userId.toString());
        }

        Passenger passenger = Passenger.create(userId, firstName, lastName);
        passengerRepository.save(passenger);

        return passenger;
    }
}
