package com.mancy.quizplatform.controller;

import com.mancy.quizplatform.security.DashboardRedirect;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** "/" shows the landing page, or sends a logged-in user to their own dashboard. */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (DashboardRedirect.isLoggedIn(authentication)) {
            return "redirect:" + DashboardRedirect.pathFor(authentication);
        }
        return "index";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
