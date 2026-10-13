package com.mancy.quizplatform.controller;

import com.mancy.quizplatform.dto.SettingsForm;
import com.mancy.quizplatform.entity.AppUser;
import com.mancy.quizplatform.enums.QuizStatus;
import com.mancy.quizplatform.enums.Role;
import com.mancy.quizplatform.exception.BusinessRuleException;
import com.mancy.quizplatform.service.AdminService;
import com.mancy.quizplatform.service.QuizApprovalService;
import com.mancy.quizplatform.service.SettingsService;
import com.mancy.quizplatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Admin pages. Every URL under /admin/** is restricted to the ADMIN role in SecurityConfig. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final QuizApprovalService approvalService;
    private final SettingsService settingsService;

    public AdminController(AdminService adminService, UserService userService,
                           QuizApprovalService approvalService, SettingsService settingsService) {
        this.adminService = adminService;
        this.userService = userService;
        this.approvalService = approvalService;
        this.settingsService = settingsService;
    }

    // ------------------------------------------------------------ dashboard

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getStats());
        model.addAttribute("pendingQuizzes", approvalService.list(QuizStatus.PENDING_APPROVAL));
        return "admin/dashboard";
    }

    // ------------------------------------------------------------ users

    @GetMapping("/users")
    public String users(@RequestParam(name = "role", required = false) Role role, Model model) {
        model.addAttribute("users", userService.listUsers(role));
        model.addAttribute("roles", Role.values());
        model.addAttribute("selectedRole", role);
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable("id") Long id, @RequestParam("role") Role role,
                             Principal principal, RedirectAttributes redirectAttributes) {
        try {
            AppUser user = userService.changeRole(id, role, principal.getName());
            redirectAttributes.addFlashAttribute("success",
                    user.getFullName() + " is now: " + user.getRole().getLabel() + ".");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleEnabled(@PathVariable("id") Long id, Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            AppUser user = userService.toggleEnabled(id, principal.getName());
            redirectAttributes.addFlashAttribute("success",
                    user.getFullName() + (user.isEnabled() ? " was enabled." : " was disabled."));
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ------------------------------------------------------------ settings

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("settingsForm", settingsService.getForm());
        return "admin/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@Valid @ModelAttribute("settingsForm") SettingsForm form, BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/settings";
        }
        settingsService.update(form);
        redirectAttributes.addFlashAttribute("success", "Settings saved.");
        return "redirect:/admin/settings";
    }
}
