package akendo.passengerservice.repository;

import akendo.passengerservice.domain.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, UUID> {
    boolean existsByUserId(UUID userId);
}
