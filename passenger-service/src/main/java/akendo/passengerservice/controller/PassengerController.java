package akendo.passengerservice.controller;
import akendo.passengerservice.domain.Passenger;
import akendo.passengerservice.dto.CreatePassengerRequest;
import akendo.passengerservice.dto.CreatePassengerResponse;
import akendo.passengerservice.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/passengers")
public class PassengerController {
    private final PassengerService passengerService;

    @PostMapping()
    public CreatePassengerResponse create(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreatePassengerRequest request) {
        Passenger passenger = passengerService.create(
                userId,
                request.firstName(),
                request.lastName()
        );

        return new CreatePassengerResponse(
                passenger.getId(),
                passenger.getUserId(),
                passenger.getFirstName(),
                passenger.getLastName(),
                passenger.getRating(),
                passenger.getCreatedAt()
        );
    }

}
