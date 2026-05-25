package com.smd.repository;

import com.smd.entity.Notification;
import com.smd.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    List<Notification> findByRecipientAndSyllabusId(User recipient, Long syllabusId);
}
