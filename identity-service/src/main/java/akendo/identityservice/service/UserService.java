package akendo.identityservice.service;

import akendo.identityservice.domain.User;
import akendo.identityservice.exception.UserNotFoundException;
import akendo.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User create(String email, String password) {
        if(userRepository.existsByEmail(email)) {
            throw new UserNotFoundException(email);
        }

        User user =  User.create(email, password);

        return user;
    }

    public User findByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UserNotFoundException(email));

        return user;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
