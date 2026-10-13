package com.mancy.quizplatform.service;

import com.mancy.quizplatform.entity.AppUser;
import com.mancy.quizplatform.entity.Notification;
import com.mancy.quizplatform.enums.Role;
import com.mancy.quizplatform.repository.AppUserRepository;
import com.mancy.quizplatform.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Simple in-app notifications (no emails are sent). */
@Service
public class NotificationService {

    private final NotificationRepository notifications;
    private final AppUserRepository users;

    public NotificationService(NotificationRepository notifications, AppUserRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    @Transactional
    public void notifyUser(AppUser recipient, String message, String link) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setLink(link);
        notifications.save(notification);
    }

    @Transactional
    public void notifyAdmins(String message, String link) {
        for (AppUser admin : users.findByRole(Role.ADMIN)) {
            notifyUser(admin, message, link);
        }
    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        return users.findByEmail(email)
                .map(user -> notifications.countByRecipientIdAndSeenFalse(user.getId()))
                .orElse(0L);
    }

    @Transactional(readOnly = true)
    public List<Notification> listFor(String email) {
        return users.findByEmail(email)
                .map(user -> notifications.findByRecipientIdOrderByCreatedAtDesc(user.getId()))
                .orElse(List.of());
    }

    @Transactional
    public void markAllRead(String email) {
        users.findByEmail(email).ifPresent(user -> {
            List<Notification> unread = notifications.findByRecipientIdAndSeenFalse(user.getId());
            for (Notification notification : unread) {
                notification.setSeen(true);
            }
            notifications.saveAll(unread);
        });
    }
}
