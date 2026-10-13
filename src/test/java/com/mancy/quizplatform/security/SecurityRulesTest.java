package com.mancy.quizplatform.security;

import com.mancy.quizplatform.entity.AppUser;
import com.mancy.quizplatform.enums.Role;
import com.mancy.quizplatform.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Checks the URL rules (who may open what) and the registration form. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityRulesTest {

    @Autowired private MockMvc mvc;
    @Autowired private AppUserRepository users;

    @Test
    void anonymousVisitorIsSentToLogin() throws Exception {
        mvc.perform(get("/admin/dashboard")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/participant/dashboard")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/notifications")).andExpect(status().is3xxRedirection());
    }

    @Test
    void publicPagesAreOpen() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "p@test.example", roles = "PARTICIPANT")
    void participantCannotOpenAdminOrCreatorPages() throws Exception {
        mvc.perform(get("/admin/dashboard")).andExpect(status().isForbidden());
        mvc.perform(get("/admin/users")).andExpect(status().isForbidden());
        mvc.perform(get("/creator/dashboard")).andExpect(status().isForbidden());
        mvc.perform(post("/admin/quizzes/1/approve").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "c@test.example", roles = "CREATOR")
    void creatorCannotManageUsersOrTakeQuizzes() throws Exception {
        mvc.perform(get("/admin/users")).andExpect(status().isForbidden());
        mvc.perform(post("/admin/users/1/role").with(csrf()).param("role", "ADMIN")).andExpect(status().isForbidden());
        mvc.perform(get("/participant/quizzes")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "a@test.example", roles = "ADMIN")
    void adminCannotUseParticipantPages() throws Exception {
        mvc.perform(get("/participant/dashboard")).andExpect(status().isForbidden());
    }

    @Test
    void postWithoutCsrfTokenIsRejected() throws Exception {
        mvc.perform(post("/register").param("fullName", "X")).andExpect(status().isForbidden());
    }

    @Test
    void weakPasswordIsRejectedByValidation() throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("fullName", "Weak Password")
                        .param("email", "weak@test.example")
                        .param("password", "abc")
                        .param("confirmPassword", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "password"));
    }

    @Test
    void mismatchedPasswordsAreRejected() throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("fullName", "Mismatch")
                        .param("email", "mismatch@test.example")
                        .param("password", "Password1")
                        .param("confirmPassword", "Password2"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "confirmPassword"));
    }

    @Test
    void registrationAlwaysCreatesAParticipantEvenIfRoleIsInjected() throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("fullName", "Sneaky User")
                        .param("email", "sneaky@test.example")
                        .param("password", "Password1")
                        .param("confirmPassword", "Password1")
                        .param("role", "ADMIN"))          // extra field the real form does not have
                .andExpect(redirectedUrl("/login?registered"));

        AppUser saved = users.findByEmail("sneaky@test.example").orElseThrow();
        assertEquals(Role.PARTICIPANT, saved.getRole());
        assertTrue(saved.getPasswordHash().startsWith("$2"), "password must be stored as a BCrypt hash");
    }
}
