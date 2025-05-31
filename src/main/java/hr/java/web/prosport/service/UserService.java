package hr.java.web.prosport.service;

import hr.java.web.prosport.dto.UserDto;

public interface UserService {
    UserDto registerUser(UserDto userDTO);
    UserDto findByUsername(String username);
}