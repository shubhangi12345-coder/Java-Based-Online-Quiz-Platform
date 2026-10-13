package com.mancy.quizplatform.controller;

import com.mancy.quizplatform.dto.QuestionForm;
import com.mancy.quizplatform.dto.QuizForm;
import com.mancy.quizplatform.entity.Quiz;
import com.mancy.quizplatform.enums.Difficulty;
import com.mancy.quizplatform.exception.BusinessRuleException;
import com.mancy.quizplatform.service.CreatorQuizService;
import com.mancy.quizplatform.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Quiz Creator pages. /creator/** is restricted to CREATOR in SecurityConfig;
 * "who owns this quiz" is checked inside CreatorQuizService.
 */
@Controller
@RequestMapping("/creator")
public class CreatorController {

    private final CreatorQuizService service;
    private final SettingsService settings;

    public CreatorController(CreatorQuizService service, SettingsService settings) {
        this.service = service;
        this.settings = settings;
    }

    // ------------------------------------------------------------ dashboard and list

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        List<Quiz> mine = service.listMine(principal.getName());
        model.addAttribute("stats", service.getDashboardStats(principal.getName()));
        model.addAttribute("recentQuizzes", mine.subList(0, Math.min(5, mine.size())));
        return "creator/dashboard";
    }

    @GetMapping("/quizzes")
    public String list(Principal principal, Model model) {
        model.addAttribute("quizzes", service.listMine(principal.getName()));
        return "creator/quizzes";
    }

    // ------------------------------------------------------------ create / edit quiz

    @GetMapping("/quizzes/new")
    public String newQuizForm(Model model) {
        QuizForm form = new QuizForm();
        form.setPassingPercent(settings.getDefaultPassingPercent());
        model.addAttribute("quizForm", form);
        prepareQuizForm(model, "New quiz", "/creator/quizzes/new", "/creator/quizzes");
        return "creator/quiz-form";
    }

    @PostMapping("/quizzes/new")
    public String createQuiz(@Valid @ModelAttribute("quizForm") QuizForm form, BindingResult result,
                             Principal principal, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            prepareQuizForm(model, "New quiz", "/creator/quizzes/new", "/creator/quizzes");
            return "creator/quiz-form";
        }
        Quiz quiz = service.createQuiz(form, principal.getName());
        redirectAttributes.addFlashAttribute("success", "Draft saved. Now add your questions.");
        return "redirect:/creator/quizzes/" + quiz.getId();
    }

    @GetMapping("/quizzes/{id}/edit")
    public String editQuizForm(@PathVariable("id") Long id, Principal principal, Model model,
                               RedirectAttributes redirectAttributes) {
        Quiz quiz = service.getOwnedQuiz(id, principal.getName());
        if (!quiz.isEditable()) {
            redirectAttributes.addFlashAttribute("error", "Only draft or rejected quizzes can be edited.");
            return "redirect:/creator/quizzes/" + id;
        }
        model.addAttribute("quizForm", service.toForm(quiz));
        prepareQuizForm(model, "Edit quiz", "/creator/quizzes/" + id + "/edit", "/creator/quizzes/" + id);
        return "creator/quiz-form";
    }

    @PostMapping("/quizzes/{id}/edit")
    public String updateQuiz(@PathVariable("id") Long id, @Valid @ModelAttribute("quizForm") QuizForm form,
                             BindingResult result, Principal principal, Model model,
                             RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                service.updateQuiz(id, form, principal.getName());
                redirectAttributes.addFlashAttribute("success", "Quiz saved.");
                return "redirect:/creator/quizzes/" + id;
            } catch (BusinessRuleException ex) {
                result.reject("rule", ex.getMessage());
            }
        }
        prepareQuizForm(model, "Edit quiz", "/creator/quizzes/" + id + "/edit", "/creator/quizzes/" + id);
        return "creator/quiz-form";
    }

    // ------------------------------------------------------------ manage one quiz

    @GetMapping("/quizzes/{id}")
    public String manage(@PathVariable("id") Long id, Principal principal, Model model) {
        model.addAttribute("quiz", service.getOwnedQuiz(id, principal.getName()));
        model.addAttribute("minQuestions", CreatorQuizService.MIN_QUESTIONS_TO_SUBMIT);
        return "creator/quiz-manage";
    }

    @PostMapping("/quizzes/{id}/submit")
    public String submit(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            service.submitForApproval(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Quiz submitted. Waiting for admin approval.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/creator/quizzes/" + id;
    }

    @PostMapping("/quizzes/{id}/delete")
    public String delete(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            service.deleteQuiz(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Quiz deleted.");
            return "redirect:/creator/quizzes";
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/creator/quizzes/" + id;
        }
    }

    // ------------------------------------------------------------ questions

    @GetMapping("/quizzes/{id}/questions/new")
    public String newQuestionForm(@PathVariable("id") Long id, Principal principal, Model model,
                                  RedirectAttributes redirectAttributes) {
        Quiz quiz = service.getOwnedQuiz(id, principal.getName());
        if (!quiz.isEditable()) {
            redirectAttributes.addFlashAttribute("error", "Only draft or rejected quizzes can be edited.");
            return "redirect:/creator/quizzes/" + id;
        }
        model.addAttribute("questionForm", new QuestionForm());
        prepareQuestionForm(model, quiz, "Add question", "/creator/quizzes/" + id + "/questions/new");
        return "creator/question-form";
    }

    @PostMapping("/quizzes/{id}/questions/new")
    public String addQuestion(@PathVariable("id") Long id, @Valid @ModelAttribute("questionForm") QuestionForm form,
                              BindingResult result, Principal principal, Model model,
                              RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                service.addQuestion(id, form, principal.getName());
                redirectAttributes.addFlashAttribute("success", "Question added.");
                return "redirect:/creator/quizzes/" + id;
            } catch (BusinessRuleException ex) {
                result.reject("rule", ex.getMessage());
            }
        }
        prepareQuestionForm(model, service.getOwnedQuiz(id, principal.getName()), "Add question",
                "/creator/quizzes/" + id + "/questions/new");
        return "creator/question-form";
    }

    @GetMapping("/quizzes/{id}/questions/{questionId}/edit")
    public String editQuestionForm(@PathVariable("id") Long id, @PathVariable("questionId") Long questionId,
                                   Principal principal, Model model, RedirectAttributes redirectAttributes) {
        Quiz quiz = service.getOwnedQuiz(id, principal.getName());
        if (!quiz.isEditable()) {
            redirectAttributes.addFlashAttribute("error", "Only draft or rejected quizzes can be edited.");
            return "redirect:/creator/quizzes/" + id;
        }
        model.addAttribute("questionForm", service.getQuestionForm(id, questionId, principal.getName()));
        prepareQuestionForm(model, quiz, "Edit question",
                "/creator/quizzes/" + id + "/questions/" + questionId + "/edit");
        return "creator/question-form";
    }

    @PostMapping("/quizzes/{id}/questions/{questionId}/edit")
    public String updateQuestion(@PathVariable("id") Long id, @PathVariable("questionId") Long questionId,
                                 @Valid @ModelAttribute("questionForm") QuestionForm form, BindingResult result,
                                 Principal principal, Model model, RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                service.updateQuestion(id, questionId, form, principal.getName());
                redirectAttributes.addFlashAttribute("success", "Question updated.");
                return "redirect:/creator/quizzes/" + id;
            } catch (BusinessRuleException ex) {
                result.reject("rule", ex.getMessage());
            }
        }
        prepareQuestionForm(model, service.getOwnedQuiz(id, principal.getName()), "Edit question",
                "/creator/quizzes/" + id + "/questions/" + questionId + "/edit");
        return "creator/question-form";
    }

    @PostMapping("/quizzes/{id}/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable("id") Long id, @PathVariable("questionId") Long questionId,
                                 Principal principal, RedirectAttributes redirectAttributes) {
        try {
            service.deleteQuestion(id, questionId, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Question deleted.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/creator/quizzes/" + id;
    }

    // ------------------------------------------------------------ results

    @GetMapping("/quizzes/{id}/results")
    public String results(@PathVariable("id") Long id, Principal principal, Model model) {
        model.addAttribute("quiz", service.getOwnedQuiz(id, principal.getName()));
        model.addAttribute("stats", service.getQuizStats(id, principal.getName()));
        model.addAttribute("attempts", service.getFinishedAttempts(id, principal.getName()));
        model.addAttribute("feedbackList", service.getFeedback(id, principal.getName()));
        return "creator/results";
    }

    // ------------------------------------------------------------ helpers

    private void prepareQuizForm(Model model, String title, String action, String cancelUrl) {
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("cancelUrl", cancelUrl);
        model.addAttribute("difficulties", Difficulty.values());
    }

    private void prepareQuestionForm(Model model, Quiz quiz, String title, String action) {
        model.addAttribute("quiz", quiz);
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("difficulties", Difficulty.values());
    }
}
