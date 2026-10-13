package com.mancy.quizplatform.controller;

import com.mancy.quizplatform.enums.QuizStatus;
import com.mancy.quizplatform.exception.BusinessRuleException;
import com.mancy.quizplatform.service.QuizApprovalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin: review, approve and reject quizzes. */
@Controller
@RequestMapping("/admin/quizzes")
public class AdminQuizController {

    private final QuizApprovalService approvalService;

    public AdminQuizController(QuizApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    public String list(@RequestParam(name = "status", required = false) QuizStatus status, Model model) {
        model.addAttribute("quizzes", approvalService.list(status));
        model.addAttribute("statuses", QuizStatus.values());
        model.addAttribute("selectedStatus", status);
        return "admin/quizzes";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable("id") Long id, Model model) {
        model.addAttribute("quiz", approvalService.get(id));
        return "admin/quiz-detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            approvalService.approve(id);
            redirectAttributes.addFlashAttribute("success", "Quiz approved. Participants can now see it.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/quizzes/" + id;
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable("id") Long id,
                         @RequestParam(name = "reason", required = false, defaultValue = "") String reason,
                         RedirectAttributes redirectAttributes) {
        try {
            approvalService.reject(id, reason);
            redirectAttributes.addFlashAttribute("success", "Quiz rejected. The creator was notified.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/quizzes/" + id;
    }
}
