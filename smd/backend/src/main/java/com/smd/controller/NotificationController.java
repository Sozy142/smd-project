package com.smd.controller;

import com.smd.entity.Notification;
import com.smd.entity.User;
import com.smd.exception.ApiException;
import com.smd.repository.NotificationRepository;
import com.smd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationController(NotificationRepository notificationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getNotifications(Principal principal) {
        User user = findUser(principal.getName());
        List<Map<String, Object>> result = notificationRepository
                .findByRecipientOrderByCreatedAtDesc(user)
                .stream()
                .map(n -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", n.getId());
                    m.put("message", n.getMessage());
                    m.put("isRead", n.isRead());
                    m.put("syllabusId", n.getSyllabusId());
                    m.put("link", n.getLink());
                    m.put("createdAt", n.getCreatedAt());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/read-by-syllabus/{syllabusId}")
    public ResponseEntity<Void> markReadBySyllabus(@PathVariable Long syllabusId, Principal principal) {
        User user = findUser(principal.getName());
        List<Notification> notifs = notificationRepository.findByRecipientAndSyllabusId(user, syllabusId);
        notifs.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifs);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(Principal principal) {
        User user = findUser(principal.getName());
        List<Notification> notifs = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        notifs.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifs);
        return ResponseEntity.noContent().build();
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
