package com.mancy.quizplatform.repository;

import com.mancy.quizplatform.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findByRecipientIdAndSeenFalse(Long recipientId);

    long countByRecipientIdAndSeenFalse(Long recipientId);
}
