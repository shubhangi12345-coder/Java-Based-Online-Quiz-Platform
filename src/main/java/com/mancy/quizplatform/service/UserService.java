package com.mancy.quizplatform.service;

import com.mancy.quizplatform.dto.RegistrationForm;
import com.mancy.quizplatform.entity.AppUser;
import com.mancy.quizplatform.enums.Role;
import com.mancy.quizplatform.exception.BusinessRuleException;
import com.mancy.quizplatform.exception.DuplicateEmailException;
import com.mancy.quizplatform.exception.ResourceNotFoundException;
import com.mancy.quizplatform.repository.AppUserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Business rules for users: registration and the admin's user management. */
@Service
public class UserService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final SettingsService settings;

    public UserService(AppUserRepository users, PasswordEncoder passwordEncoder, SettingsService settings) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.settings = settings;
    }

    /** Public registration. Everyone who registers here becomes a PARTICIPANT, whatever the form says. */
    @Transactional
    public AppUser register(RegistrationForm form) {
        if (!settings.isRegistrationOpen()) {
            throw new BusinessRuleException("Registration is currently closed. Please contact the administrator.");
        }
        if (form.getPassword() == null || !form.getPassword().equals(form.getConfirmPassword())) {
            throw new BusinessRuleException("Passwords do not match.");
        }
        return create(form.getFullName(), form.getEmail(), form.getPassword(), Role.PARTICIPANT);
    }

    /** Creates a user with the given role. The password is hashed with BCrypt before saving. */
    @Transactional
    public AppUser create(String fullName, String email, String rawPassword, Role role) {
        String cleanEmail = email.trim().toLowerCase();
        if (users.existsByEmail(cleanEmail)) {
            throw new DuplicateEmailException(cleanEmail);
        }
        AppUser user = new AppUser();
        user.setFullName(fullName.trim());
        user.setEmail(cleanEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        try {
            return users.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            // two people registering the same email at the same moment: the UNIQUE constraint caught it
            throw new DuplicateEmailException(cleanEmail);
        }
    }

    public Optional<AppUser> findOptionalByEmail(String email) {
        return users.findByEmail(email);
    }

    public AppUser findByEmail(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    public AppUser findById(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    /** All users (newest first), optionally only one role. */
    @Transactional(readOnly = true)
    public List<AppUser> listUsers(Role role) {
        return role == null ? users.findAllByOrderByCreatedAtDesc() : users.findByRoleOrderByCreatedAtDesc(role);
    }

    /** Admin changes another user's role. Admins cannot change their own role (so the last admin cannot be lost). */
    @Transactional
    public AppUser changeRole(Long userId, Role newRole, String actingEmail) {
        AppUser user = findById(userId);
        if (user.getEmail().equalsIgnoreCase(actingEmail)) {
            throw new BusinessRuleException("You cannot change your own role.");
        }
        user.setRole(newRole);
        return users.save(user);
    }

    /** Admin enables or disables another user's account. Disabled users cannot log in. */
    @Transactional
    public AppUser toggleEnabled(Long userId, String actingEmail) {
        AppUser user = findById(userId);
        if (user.getEmail().equalsIgnoreCase(actingEmail)) {
            throw new BusinessRuleException("You cannot disable your own account.");
        }
        user.setEnabled(!user.isEnabled());
        return users.save(user);
    }
}
