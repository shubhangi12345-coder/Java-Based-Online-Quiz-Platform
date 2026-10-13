package com.mancy.quizplatform.controller;

import com.mancy.quizplatform.service.NotificationService;
import com.mancy.quizplatform.service.UserService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/** Adds "currentUser" and "unreadCount" to every page, so the navigation bar can show them. */
@ControllerAdvice
public class GlobalModelAdvice {

    private final UserService userService;
    private final NotificationService notificationService;

    public GlobalModelAdvice(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void addCommonAttributes(Principal principal, Model model) {
        if (principal != null) {
            userService.findOptionalByEmail(principal.getName())
                    .ifPresent(user -> model.addAttribute("currentUser", user));
            model.addAttribute("unreadCount", notificationService.unreadCount(principal.getName()));
        }
    }
}
