package com.smd.controller;

import com.smd.dto.ReviewDecisionRequest;
import com.smd.dto.SyllabusRequest;
import com.smd.dto.SyllabusResponse;
import com.smd.service.SyllabusService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/syllabi")
public class SyllabusController {

    private final SyllabusService syllabusService;

    @Autowired
    public SyllabusController(SyllabusService syllabusService) {
        this.syllabusService = syllabusService;
    }

    @PostMapping
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<SyllabusResponse> create(@Valid @RequestBody SyllabusRequest req,
                                                    Principal principal) {
        return ResponseEntity.ok(syllabusService.create(req, principal.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<SyllabusResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody SyllabusRequest req,
                                                    Principal principal) {
        return ResponseEntity.ok(syllabusService.update(id, req, principal.getName()));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<SyllabusResponse> submit(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(syllabusService.submit(id, principal.getName()));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<List<SyllabusResponse>> listMine(Principal principal) {
        return ResponseEntity.ok(syllabusService.listMine(principal.getName()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('HOD','PRINCIPAL')")
    public ResponseEntity<List<SyllabusResponse>> listPending() {
        return ResponseEntity.ok(syllabusService.listPendingReview());
    }

    @GetMapping("/pending-approval")
    @PreAuthorize("hasAnyRole('ACADEMIC_AFFAIRS','PRINCIPAL')")
    public ResponseEntity<List<SyllabusResponse>> listPendingApproval() {
        return ResponseEntity.ok(syllabusService.listPendingApproval());
    }

    @GetMapping("/department-search")
    @PreAuthorize("hasRole('HOD')")
    public ResponseEntity<List<SyllabusResponse>> departmentSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Principal principal) {
        return ResponseEntity.ok(syllabusService.searchByDepartment(keyword, status, principal.getName()));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ACADEMIC_AFFAIRS','ADMIN','PRINCIPAL')")
    public ResponseEntity<List<SyllabusResponse>> searchAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(syllabusService.searchAll(keyword, status));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('HOD')")
    public ResponseEntity<SyllabusResponse> approve(@PathVariable Long id,
                                                     @RequestBody ReviewDecisionRequest req,
                                                     Principal principal) {
        return ResponseEntity.ok(syllabusService.approve(id, req.getComments(), principal.getName()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('HOD')")
    public ResponseEntity<SyllabusResponse> reject(@PathVariable Long id,
                                                    @RequestBody ReviewDecisionRequest req,
                                                    Principal principal) {
        return ResponseEntity.ok(syllabusService.reject(id, req.getComments(), principal.getName()));
    }

    @PostMapping("/{id}/aa-approve")
    @PreAuthorize("hasRole('ACADEMIC_AFFAIRS')")
    public ResponseEntity<SyllabusResponse> aaApprove(@PathVariable Long id,
                                                       @RequestBody ReviewDecisionRequest req,
                                                       Principal principal) {
        return ResponseEntity.ok(syllabusService.aaApprove(id, req.getComments(), principal.getName()));
    }

    @PostMapping("/{id}/aa-reject")
    @PreAuthorize("hasRole('ACADEMIC_AFFAIRS')")
    public ResponseEntity<SyllabusResponse> aaReject(@PathVariable Long id,
                                                      @RequestBody ReviewDecisionRequest req,
                                                      Principal principal) {
        return ResponseEntity.ok(syllabusService.aaReject(id, req.getComments(), principal.getName()));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL')")
    public ResponseEntity<SyllabusResponse> publish(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(syllabusService.publish(id, principal.getName()));
    }

    @PostMapping("/{id}/new-version")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<SyllabusResponse> newVersion(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(syllabusService.newVersion(id, principal.getName()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL','ACADEMIC_AFFAIRS')")
    public ResponseEntity<List<SyllabusResponse>> listAll() {
        return ResponseEntity.ok(syllabusService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SyllabusResponse> getById(@PathVariable Long id, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(syllabusService.getById(id, email));
    }

    @GetMapping("/{id}/ai-summary")
    public ResponseEntity<String> aiSummary(@PathVariable Long id) {
        return ResponseEntity.ok(syllabusService.aiSummary(id));
    }

    @GetMapping("/{id}/versions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> listVersions(@PathVariable Long id) {
        return ResponseEntity.ok(syllabusService.listVersions(id));
    }

    @GetMapping("/{id}/ai-compare")
    @PreAuthorize("hasAnyRole('HOD','ACADEMIC_AFFAIRS','ADMIN','PRINCIPAL','LECTURER')")
    public ResponseEntity<Map<String, Object>> aiCompare(@PathVariable Long id,
                                                          @RequestParam Long versionId1,
                                                          @RequestParam Long versionId2) {
        return ResponseEntity.ok(syllabusService.aiCompare(id, versionId1, versionId2));
    }
}
