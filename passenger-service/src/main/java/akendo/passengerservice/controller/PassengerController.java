package akendo.passengerservice.controller;
import akendo.passengerservice.domain.Passenger;
import akendo.passengerservice.dto.CreatePassengerRequest;
import akendo.passengerservice.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/passengers")
public class PassengerController {
    private final PassengerService passengerService;

    @PostMapping("/create")
    public Passenger create(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody CreatePassengerRequest request) {
        return passengerService.create(
                userId,
                request.firstName(),
                request.lastName()
        );
    }

}
