package hr.java.web.prosport.service;

import hr.java.web.prosport.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserDto registerUser(UserDto userDTO);
    UserDto findByUsername(String username);
}