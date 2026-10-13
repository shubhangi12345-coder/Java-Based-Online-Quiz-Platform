package com.mancy.quizplatform.controller;

import com.mancy.quizplatform.dto.FeedbackForm;
import com.mancy.quizplatform.dto.ParticipantStats;
import com.mancy.quizplatform.entity.Attempt;
import com.mancy.quizplatform.entity.Feedback;
import com.mancy.quizplatform.enums.AttemptStatus;
import com.mancy.quizplatform.enums.Difficulty;
import com.mancy.quizplatform.exception.BusinessRuleException;
import com.mancy.quizplatform.service.AttemptService;
import com.mancy.quizplatform.service.FeedbackService;
import com.mancy.quizplatform.service.LeaderboardService;
import com.mancy.quizplatform.service.ParticipantDashboardService;
import com.mancy.quizplatform.service.ParticipantQuizService;
import com.mancy.quizplatform.util.ScoreCalculator;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Participant pages. /participant/** is restricted to the PARTICIPANT role in SecurityConfig. */
@Controller
@RequestMapping("/participant")
public class ParticipantController {

    private final ParticipantQuizService quizService;
    private final AttemptService attemptService;
    private final ParticipantDashboardService dashboardService;
    private final LeaderboardService leaderboardService;
    private final FeedbackService feedbackService;

    public ParticipantController(ParticipantQuizService quizService, AttemptService attemptService,
                                 ParticipantDashboardService dashboardService,
                                 LeaderboardService leaderboardService, FeedbackService feedbackService) {
        this.quizService = quizService;
        this.attemptService = attemptService;
        this.dashboardService = dashboardService;
        this.leaderboardService = leaderboardService;
        this.feedbackService = feedbackService;
    }

    // ------------------------------------------------------------ dashboard

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        List<Attempt> history = attemptService.history(principal.getName());
        model.addAttribute("stats", dashboardService.getStats(principal.getName()));
        model.addAttribute("recentAttempts", history.subList(0, Math.min(5, history.size())));
        model.addAttribute("runningAttempt", findRunning(history, null));
        return "participant/dashboard";
    }

    // ------------------------------------------------------------ browse and details

    @GetMapping("/quizzes")
    public String browse(@RequestParam(name = "keyword", required = false) String keyword,
                         @RequestParam(name = "difficulty", required = false) Difficulty difficulty,
                         Principal principal, Model model) {
        ParticipantStats stats = dashboardService.getStats(principal.getName());
        model.addAttribute("quizzes", quizService.browse(keyword, difficulty));
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedDifficulty", difficulty);
        model.addAttribute("recommended", stats.getRecommendedDifficulty());
        return "participant/quizzes";
    }

    @GetMapping("/quizzes/{id}")
    public String details(@PathVariable("id") Long id, Principal principal, Model model) {
        List<Attempt> history = attemptService.history(principal.getName());
        List<Attempt> finishedForQuiz = new ArrayList<>();
        for (Attempt attempt : history) {
            if (attempt.getQuiz().getId().equals(id) && attempt.isFinished()) {
                finishedForQuiz.add(attempt);
            }
        }
        model.addAttribute("quiz", quizService.getApprovedQuiz(id));
        model.addAttribute("runningAttempt", findRunning(history, id));
        model.addAttribute("pastAttempts", finishedForQuiz);
        return "participant/quiz-detail";
    }

    /** POST (not GET), so links, refreshes and browser prefetching can never start a quiz by accident. */
    @PostMapping("/quizzes/{id}/start")
    public String start(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            Attempt attempt = attemptService.start(id, principal.getName());
            return "redirect:/participant/attempts/" + attempt.getId() + "/take";
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/participant/quizzes/" + id;
        }
    }

    // ------------------------------------------------------------ taking the quiz

    @GetMapping("/attempts/{id}/take")
    public String take(@PathVariable("id") Long id, Principal principal, Model model,
                       RedirectAttributes redirectAttributes) {
        Attempt attempt = attemptService.openForTaking(id, principal.getName());
        if (attempt.isFinished()) {
            if (attempt.getStatus() == AttemptStatus.AUTO_SUBMITTED) {
                redirectAttributes.addFlashAttribute("warning", "Time was up, so the quiz was submitted automatically.");
            }
            return "redirect:/participant/attempts/" + id + "/result";
        }
        long remaining = Math.max(1, Duration.between(LocalDateTime.now(), attempt.getDeadline()).getSeconds());
        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", attempt.getQuiz());
        model.addAttribute("remainingSeconds", remaining);
        return "participant/take";
    }

    /**
     * Final submit. The browser sends only the CHOSEN OPTION IDS (fields named q_<questionId>).
     * It never sends a score: the score is calculated on the server.
     */
    @PostMapping("/attempts/{id}/submit")
    public String submit(@PathVariable("id") Long id, @RequestParam Map<String, String> params,
                         Principal principal, RedirectAttributes redirectAttributes) {
        Map<Long, Long> selections = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().startsWith("q_")) {
                continue;
            }
            try {
                selections.put(Long.valueOf(entry.getKey().substring(2)), Long.valueOf(entry.getValue()));
            } catch (NumberFormatException ex) {
                // not a valid question / option id: ignore it
            }
        }
        Attempt attempt = attemptService.submit(id, principal.getName(), selections);
        if (attempt.getStatus() == AttemptStatus.AUTO_SUBMITTED) {
            redirectAttributes.addFlashAttribute("warning", "Time was up, so the quiz was submitted automatically.");
        } else {
            redirectAttributes.addFlashAttribute("success", "Quiz submitted.");
        }
        return "redirect:/participant/attempts/" + id + "/result";   // redirect: a refresh never submits twice
    }

    // ------------------------------------------------------------ result, history, leaderboard

    @GetMapping("/attempts/{id}/result")
    public String result(@PathVariable("id") Long id, Principal principal, Model model) {
        Attempt attempt;
        try {
            attempt = attemptService.getFinishedAttempt(id, principal.getName());
        } catch (BusinessRuleException ex) {
            return "redirect:/participant/attempts/" + id + "/take";   // still running: back to the quiz
        }
        Optional<Feedback> mine = feedbackService.findMine(attempt.getQuiz().getId(), principal.getName());
        FeedbackForm form = new FeedbackForm();
        mine.ifPresent(feedback -> {
            form.setRating(feedback.getRating());
            form.setComment(feedback.getComment());
        });
        model.addAttribute("attempt", attempt);
        model.addAttribute("grade", ScoreCalculator.grade(attempt.getPercentage()));
        model.addAttribute("feedbackForm", form);
        model.addAttribute("hasFeedback", mine.isPresent());
        return "participant/result";
    }

    @GetMapping("/attempts")
    public String history(Principal principal, Model model) {
        model.addAttribute("attempts", attemptService.history(principal.getName()));
        return "participant/history";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Principal principal, Model model) {
        model.addAttribute("rows", leaderboardService.getRanking());
        return "participant/leaderboard";
    }

    // ------------------------------------------------------------ feedback

    @PostMapping("/quizzes/{id}/feedback")
    public String feedback(@PathVariable("id") Long id, @Valid @ModelAttribute("feedbackForm") FeedbackForm form,
                           BindingResult result, @RequestParam(name = "attemptId") Long attemptId,
                           Principal principal, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Choose a rating from 1 to 5 (comment max 1000 characters).");
        } else {
            try {
                feedbackService.submit(id, principal.getName(), form);
                redirectAttributes.addFlashAttribute("success", "Thank you for your feedback.");
            } catch (BusinessRuleException ex) {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
            }
        }
        return "redirect:/participant/attempts/" + attemptId + "/result";
    }

    // ------------------------------------------------------------ helpers

    /** The first unfinished attempt (optionally only for one quiz), or null. */
    private Attempt findRunning(List<Attempt> history, Long quizId) {
        for (Attempt attempt : history) {
            if (!attempt.isFinished() && (quizId == null || attempt.getQuiz().getId().equals(quizId))) {
                return attempt;
            }
        }
        return null;
    }
}
