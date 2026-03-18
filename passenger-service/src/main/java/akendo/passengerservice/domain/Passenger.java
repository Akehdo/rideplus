package akendo.passengerservice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "passenger")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "rating", updatable = true)
    private Double rating;

    @Column(name = "rides_count")
    private Integer ridesCount;

    @Column(name = "canceled_rides")
    private Integer canceledRides;

    @Column(name = "created_at")
    private Instant createdAt;

    public static Passenger create(UUID userId, String firstName, String lastName) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName is required");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName is required");
        }

        Passenger passenger = new Passenger();

        passenger.userId = userId;
        passenger.firstName = firstName;
        passenger.lastName = lastName;

        passenger.rating = 5.0;
        passenger.ridesCount = 0;
        passenger.canceledRides = 0;
        passenger.createdAt = Instant.now();

        return passenger;
    }
}
