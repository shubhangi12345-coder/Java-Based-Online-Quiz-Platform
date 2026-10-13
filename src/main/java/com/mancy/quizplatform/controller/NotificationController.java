package com.mancy.quizplatform.controller;

import com.mancy.quizplatform.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** The in-app notification list (available to every logged-in role). */
@Controller
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String list(Principal principal, Model model) {
        model.addAttribute("notifications", notificationService.listFor(principal.getName()));
        return "notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead(Principal principal, RedirectAttributes redirectAttributes) {
        notificationService.markAllRead(principal.getName());
        redirectAttributes.addFlashAttribute("success", "All notifications marked as read.");
        return "redirect:/notifications";
    }
}
