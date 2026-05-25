package com.smd.controller;

import com.smd.entity.Syllabus;
import com.smd.entity.SyllabusFollow;
import com.smd.entity.User;
import com.smd.exception.ApiException;
import com.smd.repository.SyllabusFollowRepository;
import com.smd.repository.SyllabusRepository;
import com.smd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/syllabi")
public class SyllabusFollowController {

    private final SyllabusFollowRepository followRepository;
    private final SyllabusRepository syllabusRepository;
    private final UserRepository userRepository;

    @Autowired
    public SyllabusFollowController(SyllabusFollowRepository followRepository,
                                     SyllabusRepository syllabusRepository,
                                     UserRepository userRepository) {
        this.followRepository = followRepository;
        this.syllabusRepository = syllabusRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}/follow/status")
    public ResponseEntity<Map<String, Object>> followStatus(@PathVariable Long id, Principal principal) {
        boolean following = false;
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                following = followRepository.existsByStudentIdAndSyllabusId(user.getId(), id);
            }
        }
        long count = followRepository.countBySyllabusId(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("following", following);
        result.put("followerCount", count);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/follow")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> follow(@PathVariable Long id, Principal principal) {
        User student = findUser(principal.getName());
        Syllabus syllabus = findSyllabus(id);
        if (!followRepository.existsByStudentIdAndSyllabusId(student.getId(), id)) {
            SyllabusFollow follow = new SyllabusFollow();
            follow.setStudent(student);
            follow.setSyllabus(syllabus);
            followRepository.save(follow);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/follow")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> unfollow(@PathVariable Long id, Principal principal) {
        User student = findUser(principal.getName());
        followRepository.deleteByStudentIdAndSyllabusId(student.getId(), id);
        return ResponseEntity.noContent().build();
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Syllabus findSyllabus(Long id) {
        return syllabusRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Syllabus not found"));
    }
}
