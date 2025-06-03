package hr.java.web.prosport.controller;

import hr.java.web.prosport.dto.UserDto;
import hr.java.web.prosport.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private static final String ERROR_ATTR = "error";
    private static final String SUCCESS_ATTR = "success";
    private static final String REDIRECT_REGISTER = "redirect:/register";
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String AUTH_LOGIN = "auth/login";
    private static final String AUTH_REGISTER = "auth/register";
    private static final String INVALID_USERNAME_PASSWORD = "Invalid username or password!";
    private static final String PASSWORDS_NOT_MATCH = "Passwords do not match!";
    private static final String REGISTRATION_SUCCESSFUL = "Registration successful! You can now log in.";

    private final UserService userService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute(ERROR_ATTR, INVALID_USERNAME_PASSWORD);
        }
        return AUTH_LOGIN;
    }

    @GetMapping("/register")
    public String register() {
        return AUTH_REGISTER;
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               @RequestParam String firstName,
                               @RequestParam String lastName,
                               RedirectAttributes attributes) {
        try {
            if (!password.equals(confirmPassword)) {
                attributes.addFlashAttribute(ERROR_ATTR, PASSWORDS_NOT_MATCH);
                return REDIRECT_REGISTER;
            }

            UserDto userDto = new UserDto(username, email, firstName, lastName, password);
            userService.registerUser(userDto);
            attributes.addFlashAttribute(SUCCESS_ATTR, REGISTRATION_SUCCESSFUL);
            return REDIRECT_LOGIN;

        } catch (RuntimeException e) {
            attributes.addFlashAttribute(ERROR_ATTR, e.getMessage());
            return REDIRECT_REGISTER;
        }
    }
}