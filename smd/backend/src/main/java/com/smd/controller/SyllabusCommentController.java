package com.smd.controller;

import com.smd.dto.CommentRequest;
import com.smd.dto.CommentResponse;
import com.smd.entity.Notification;
import com.smd.entity.Syllabus;
import com.smd.entity.SyllabusComment;
import com.smd.entity.User;
import com.smd.exception.ApiException;
import com.smd.repository.NotificationRepository;
import com.smd.repository.SyllabusCommentRepository;
import com.smd.repository.SyllabusRepository;
import com.smd.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/syllabi/{syllabusId}/comments")
public class SyllabusCommentController {

    private final SyllabusCommentRepository commentRepository;
    private final SyllabusRepository syllabusRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public SyllabusCommentController(SyllabusCommentRepository commentRepository,
                                      SyllabusRepository syllabusRepository,
                                      UserRepository userRepository,
                                      NotificationRepository notificationRepository) {
        this.commentRepository = commentRepository;
        this.syllabusRepository = syllabusRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    private Syllabus findSyllabus(Long id) {
        return syllabusRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Syllabus not found"));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countComments(@PathVariable Long syllabusId) {
        Syllabus syllabus = findSyllabus(syllabusId);
        return ResponseEntity.ok(commentRepository.countBySyllabus(syllabus));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long syllabusId,
                                                              Principal principal) {
        Syllabus syllabus = findSyllabus(syllabusId);
        String email = principal != null ? principal.getName() : null;
        List<CommentResponse> result = commentRepository
                .findBySyllabusOrderByCreatedAtDesc(syllabus)
                .stream().map(c -> CommentResponse.from(c, email)).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LECTURER', 'HOD', 'ACADEMIC_AFFAIRS', 'PRINCIPAL', 'ADMIN')")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long syllabusId,
                                                       @Valid @RequestBody CommentRequest req,
                                                       Principal principal) {
        Syllabus syllabus = findSyllabus(syllabusId);
        User author = findUser(principal.getName());

        SyllabusComment comment = new SyllabusComment();
        comment.setSyllabus(syllabus);
        comment.setAuthor(author);
        comment.setContent(req.getContent());
        comment.setErrorType(req.getErrorType());

        CommentResponse saved = CommentResponse.from(commentRepository.save(comment), principal.getName());

        // Load owner explicitly to avoid LazyInitializationException on the LAZY proxy
        User owner = null;
        if (syllabus.getCreatedBy() != null) {
            Long ownerId = syllabus.getCreatedBy().getId(); // proxy ID — no SQL needed
            owner = userRepository.findById(ownerId).orElse(null);
        }

        System.out.println("[Notification] syllabus=" + syllabus.getCourseCode()
                + " owner=" + (owner != null ? owner.getEmail() : "null")
                + " commenter=" + author.getEmail());

        if (owner != null && !owner.getId().equals(author.getId())) {
            try {
                Notification notif = new Notification();
                notif.setRecipient(owner);
                notif.setMessage("New comment on " + syllabus.getCourseCode() + " by "
                        + author.getFirstName() + " " + author.getLastName());
                notif.setSyllabusId(syllabus.getId());
                notificationRepository.save(notif);
                System.out.println("[Notification] saved for " + owner.getEmail());
            } catch (Exception e) {
                System.err.println("[Notification] failed to save: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> editComment(@PathVariable Long syllabusId,
                                                        @PathVariable Long commentId,
                                                        @Valid @RequestBody CommentRequest req,
                                                        Principal principal) {
        SyllabusComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!comment.getAuthor().getEmail().equals(principal.getName())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only edit your own comments");
        }
        comment.setContent(req.getContent());
        return ResponseEntity.ok(CommentResponse.from(commentRepository.save(comment), principal.getName()));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long syllabusId,
                                               @PathVariable Long commentId,
                                               Principal principal) {
        SyllabusComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!comment.getAuthor().getEmail().equals(principal.getName())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only delete your own comments");
        }
        commentRepository.delete(comment);
        return ResponseEntity.noContent().build();
    }
}
