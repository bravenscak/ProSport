package hr.java.web.prosport.service;

import hr.java.web.prosport.dto.UserDto;
import hr.java.web.prosport.exception.UserException;
import hr.java.web.prosport.model.User;
import hr.java.web.prosport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final String USER_NOT_FOUND_MSG = "User not found: ";
    private static final String USERNAME_EXISTS_MSG = "Username already exists";
    private static final String EMAIL_EXISTS_MSG = "Email already exists";
    private static final String USER_NOT_FOUND_SIMPLE_MSG = "User not found";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MSG + username));
    }

    @Override
    public UserDto registerUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserException(USERNAME_EXISTS_MSG);
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserException(EMAIL_EXISTS_MSG);
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setRole(User.Role.USER);

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    @Override
    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND_SIMPLE_MSG));
        return mapToDTO(user);
    }

    private UserDto mapToDTO(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole().name());
        return dto;
    }
}