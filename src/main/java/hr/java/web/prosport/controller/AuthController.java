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

    private final UserService userService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String email,
                               @RequestParam String password, @RequestParam String confirmPassword,
                               @RequestParam String firstName, @RequestParam String lastName,
                               RedirectAttributes attributes) {
        try {
            if (!password.equals(confirmPassword)) {
                attributes.addFlashAttribute("error", "Passwords do not match!");
                return "redirect:/register";
            }

            UserDto UserDto = new UserDto(username, email, firstName, lastName, password);
            userService.registerUser(UserDto);
            attributes.addFlashAttribute("success", "Registration successful! You can now log in.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}