package akendo.identityservice.seeds;

import akendo.identityservice.domain.User;
import akendo.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("seed")
@RequiredArgsConstructor
public class LoadTestUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        for (int i = 1; i <= 20; i++) {
            String email = "loadtest" + i + "@test.com";

            if (userRepository.existsByEmail(email)) {
                continue;
            }

            User user = User.create(email, passwordEncoder.encode("secret123"));
            userRepository.save(user);
        }

        System.out.println("users seed created");
    }
}
