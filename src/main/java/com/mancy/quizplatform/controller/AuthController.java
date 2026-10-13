package com.mancy.quizplatform.controller;

import com.mancy.quizplatform.dto.RegistrationForm;
import com.mancy.quizplatform.exception.BusinessRuleException;
import com.mancy.quizplatform.exception.DuplicateEmailException;
import com.mancy.quizplatform.security.DashboardRedirect;
import com.mancy.quizplatform.service.SettingsService;
import com.mancy.quizplatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;

/**
 * Shows the login and registration pages. There is no POST /login here:
 * Spring Security handles the login form submission itself.
 */
@Controller
public class AuthController {

    private final UserService userService;
    private final SettingsService settings;

    public AuthController(UserService userService, SettingsService settings) {
        this.userService = userService;
        this.settings = settings;
    }

    @GetMapping("/login")
    public String loginPage(Authentication authentication) {
        if (DashboardRedirect.isLoggedIn(authentication)) {
            return "redirect:" + DashboardRedirect.pathFor(authentication);
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Authentication authentication, Model model) {
        if (DashboardRedirect.isLoggedIn(authentication)) {
            return "redirect:" + DashboardRedirect.pathFor(authentication);
        }
        model.addAttribute("registrationForm", new RegistrationForm());
        model.addAttribute("registrationClosed", !settings.isRegistrationOpen());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult result, Model model) {
        // the "passwords match" rule involves two fields, so it is checked here
        if (!Objects.equals(form.getPassword(), form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        model.addAttribute("registrationClosed", !settings.isRegistrationOpen());
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.register(form);
        } catch (DuplicateEmailException ex) {
            result.rejectValue("email", "duplicate", ex.getMessage());
            return "register";
        } catch (BusinessRuleException ex) {
            model.addAttribute("registrationClosed", true);
            return "register";
        }
        return "redirect:/login?registered";
    }
}
