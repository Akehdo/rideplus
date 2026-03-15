package akendo.identityservice.service;

import akendo.identityservice.domain.User;
import akendo.identityservice.exception.UserAlreadyExistsException;
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
            throw new UserAlreadyExistsException(email);
        }

        User user =  User.create(email, password);

        return userRepository.save(user);
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UserNotFoundException(email));

    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
