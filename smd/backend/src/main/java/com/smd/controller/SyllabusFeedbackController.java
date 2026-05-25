package com.smd.controller;

import com.smd.entity.Notification;
import com.smd.entity.Syllabus;
import com.smd.entity.SyllabusFeedback;
import com.smd.entity.User;
import com.smd.exception.ApiException;
import com.smd.repository.NotificationRepository;
import com.smd.repository.SyllabusFeedbackRepository;
import com.smd.repository.SyllabusRepository;
import com.smd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/syllabi")
public class SyllabusFeedbackController {

    private final SyllabusFeedbackRepository feedbackRepository;
    private final SyllabusRepository syllabusRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public SyllabusFeedbackController(SyllabusFeedbackRepository feedbackRepository,
                                       SyllabusRepository syllabusRepository,
                                       UserRepository userRepository,
                                       NotificationRepository notificationRepository) {
        this.feedbackRepository = feedbackRepository;
        this.syllabusRepository = syllabusRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/{id}/feedback")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getFeedback(@PathVariable Long id) {
        List<Map<String, Object>> result = feedbackRepository
                .findBySyllabusIdOrderByCreatedAtDesc(id)
                .stream()
                .map(this::feedbackMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> submitFeedback(@PathVariable Long id,
                                                               @RequestBody Map<String, String> body,
                                                               Principal principal) {
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Content is required");
        }
        Syllabus syllabus = findSyllabus(id);
        User student = findUser(principal.getName());

        SyllabusFeedback feedback = new SyllabusFeedback();
        feedback.setSyllabus(syllabus);
        feedback.setStudent(student);
        feedback.setContent(content.trim());
        SyllabusFeedback saved = feedbackRepository.save(feedback);

        if (syllabus.getCreatedBy() != null) {
            try {
                Long ownerId = syllabus.getCreatedBy().getId();
                userRepository.findById(ownerId).ifPresent(owner -> {
                    if (!owner.getId().equals(student.getId())) {
                        Notification notif = new Notification();
                        notif.setRecipient(owner);
                        notif.setMessage("New student feedback on your syllabus "
                                + syllabus.getCourseCode() + " - " + syllabus.getCourseName());
                        notif.setSyllabusId(syllabus.getId());
                        notif.setLink("/syllabus/" + syllabus.getId());
                        notificationRepository.save(notif);
                    }
                });
            } catch (Exception e) {
                System.err.println("[Feedback Notification] failed: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(feedbackMap(saved));
    }

    private Map<String, Object> feedbackMap(SyllabusFeedback f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", f.getId());
        m.put("studentId", f.getStudent().getId());
        m.put("studentName", f.getStudent().getFirstName() + " " + f.getStudent().getLastName());
        m.put("content", f.getContent());
        m.put("createdAt", f.getCreatedAt());
        return m;
    }

    private Syllabus findSyllabus(Long id) {
        return syllabusRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Syllabus not found"));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
