package com.smd.controller;

import com.smd.dto.SyllabusResponse;
import com.smd.entity.Syllabus;
import com.smd.entity.SyllabusStatus;
import com.smd.exception.ApiException;
import com.smd.repository.SyllabusRepository;
import com.smd.service.SyllabusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final SyllabusService syllabusService;
    private final SyllabusRepository syllabusRepository;

    @Autowired
    public PublicController(SyllabusService syllabusService, SyllabusRepository syllabusRepository) {
        this.syllabusService = syllabusService;
        this.syllabusRepository = syllabusRepository;
    }

    @GetMapping("/syllabi")
    public ResponseEntity<List<SyllabusResponse>> listPublic(
            @RequestParam(required = false, defaultValue = "") String q) {
        return ResponseEntity.ok(syllabusService.listPublic(q));
    }

    @GetMapping("/syllabi/{id}")
    public ResponseEntity<SyllabusResponse> publicDetail(@PathVariable Long id) {
        SyllabusResponse resp = syllabusService.getById(id, null);
        if (resp.getStatus() != SyllabusStatus.PUBLISHED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not publicly accessible");
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/syllabi/{id}/subject-tree")
    public ResponseEntity<Map<String, Object>> subjectTree(@PathVariable Long id) {
        Syllabus current = syllabusRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Syllabus not found"));

        List<Map<String, Object>> prereqList = new ArrayList<>();
        if (current.getPrerequisites() != null && !current.getPrerequisites().isBlank()) {
            List<SyllabusStatus> visible = List.of(SyllabusStatus.PUBLISHED, SyllabusStatus.APPROVED);
            for (String raw : current.getPrerequisites().split("[,;]")) {
                String code = raw.trim();
                if (!code.isEmpty()) {
                    syllabusRepository.findByCourseCodeAndStatusIn(code, visible)
                            .stream().findFirst()
                            .ifPresent(s -> prereqList.add(courseMap(s)));
                }
            }
        }

        List<Map<String, Object>> nextList = syllabusRepository
                .findByPrerequisitesContainingAndStatus(current.getCourseCode(), SyllabusStatus.PUBLISHED)
                .stream()
                .filter(s -> !s.getId().equals(current.getId()))
                .map(this::courseMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("current", courseMap(current));
        result.put("prerequisites", prereqList);
        result.put("nextCourses", nextList);
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> courseMap(Syllabus s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("courseCode", s.getCourseCode());
        m.put("courseName", s.getCourseName());
        m.put("credits", s.getCredits());
        return m;
    }

    @GetMapping("/syllabi/{id}/ai-summary")
    public ResponseEntity<String> publicAiSummary(@PathVariable Long id) {
        SyllabusResponse resp = syllabusService.getById(id, null);
        if (resp.getStatus() != SyllabusStatus.PUBLISHED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not publicly accessible");
        }
        return ResponseEntity.ok(syllabusService.aiSummary(id));
    }
}
