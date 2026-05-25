package com.smd.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smd.dto.SyllabusRequest;
import com.smd.dto.SyllabusResponse;
import com.smd.entity.*;
import com.smd.exception.ApiException;
import com.smd.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SyllabusService {

    private final SyllabusRepository syllabusRepository;
    private final UserRepository userRepository;
    private final ApprovalHistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;
    private final SyllabusFollowRepository followRepository;
    private final SyllabusVersionRepository versionRepository;
    private final GeminiService geminiService;

    @Autowired
    public SyllabusService(SyllabusRepository syllabusRepository,
                           UserRepository userRepository,
                           ApprovalHistoryRepository historyRepository,
                           NotificationRepository notificationRepository,
                           SyllabusFollowRepository followRepository,
                           SyllabusVersionRepository versionRepository,
                           GeminiService geminiService) {
        this.syllabusRepository = syllabusRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.notificationRepository = notificationRepository;
        this.followRepository = followRepository;
        this.versionRepository = versionRepository;
        this.geminiService = geminiService;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Syllabus getSyllabus(Long id) {
        return syllabusRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Syllabus not found"));
    }

    private void applyRequest(Syllabus s, SyllabusRequest req) {
        s.setCourseCode(req.getCourseCode());
        s.setCourseName(req.getCourseName());
        s.setCredits(req.getCredits());
        s.setDepartment(req.getDepartment());
        s.setAcademicYear(req.getAcademicYear());
        s.setSemester(req.getSemester());
        s.setDescription(req.getDescription());
        s.setLearningOutcomes(req.getLearningOutcomes());
        s.setAssessmentMethods(req.getAssessmentMethods());
        s.setPrerequisites(req.getPrerequisites());
        s.setMaterials(req.getMaterials());
        s.setPloOutcomes(req.getPloOutcomes());
        s.setCloMappings(req.getCloMappings());
    }

    private void logHistory(Syllabus syllabus, User actor, String action, String comments) {
        ApprovalHistory history = ApprovalHistory.builder()
                .syllabus(syllabus)
                .actor(actor)
                .actorRole(actor.getRole())
                .action(action)
                .comments(comments)
                .build();
        historyRepository.save(history);
    }

    private void notifyFollowers(Syllabus syllabus, String message) {
        try {
            List<SyllabusFollow> follows = followRepository.findBySyllabusId(syllabus.getId());
            for (SyllabusFollow follow : follows) {
                Notification notif = new Notification();
                notif.setRecipient(follow.getStudent());
                notif.setMessage(message);
                notif.setSyllabusId(syllabus.getId());
                notif.setLink("/syllabus/" + syllabus.getId());
                notificationRepository.save(notif);
            }
        } catch (Exception e) {
            System.err.println("[Follow Notification] error: " + e.getMessage());
        }
    }

    public SyllabusResponse create(SyllabusRequest req, String email) {
        User user = getUser(email);
        Syllabus syllabus = Syllabus.builder().createdBy(user).build();
        applyRequest(syllabus, req);
        return SyllabusResponse.from(syllabusRepository.save(syllabus));
    }

    public SyllabusResponse update(Long id, SyllabusRequest req, String email) {
        User user = getUser(email);
        Syllabus syllabus = getSyllabus(id);

        if (!syllabus.getCreatedBy().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this syllabus");
        }
        if (syllabus.getStatus() != SyllabusStatus.DRAFT
                && syllabus.getStatus() != SyllabusStatus.REJECTED
                && syllabus.getStatus() != SyllabusStatus.AA_REJECTED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only DRAFT, REJECTED, or AA_REJECTED syllabi can be edited");
        }
        if (syllabus.getStatus() == SyllabusStatus.REJECTED || syllabus.getStatus() == SyllabusStatus.AA_REJECTED) {
            syllabus.setVersionNumber(syllabus.getVersionNumber() + 1);
            syllabus.setStatus(SyllabusStatus.DRAFT);
            syllabus.setRejectionReason(null);
        }
        applyRequest(syllabus, req);
        return SyllabusResponse.from(syllabusRepository.save(syllabus));
    }

    public SyllabusResponse submit(Long id, String email) {
        User user = getUser(email);
        Syllabus syllabus = getSyllabus(id);

        if (!syllabus.getCreatedBy().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this syllabus");
        }
        if (syllabus.getStatus() != SyllabusStatus.DRAFT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only DRAFT syllabi can be submitted");
        }
        syllabus.setStatus(SyllabusStatus.PENDING_REVIEW);
        syllabusRepository.save(syllabus);
        logHistory(syllabus, user, "SUBMITTED", null);
        saveSnapshot(syllabus);
        return SyllabusResponse.from(syllabus);
    }

    public SyllabusResponse approve(Long id, String comments, String email) {
        User user = getUser(email);
        Syllabus syllabus = getSyllabus(id);

        if (syllabus.getStatus() != SyllabusStatus.PENDING_REVIEW) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only PENDING_REVIEW syllabi can be approved");
        }
        syllabus.setStatus(SyllabusStatus.PENDING_APPROVAL);
        syllabus.setReviewedBy(user);
        syllabusRepository.save(syllabus);
        logHistory(syllabus, user, "HOD_APPROVED", comments);

        String aaMessage = "New syllabus pending Level 2 approval: "
                + syllabus.getCourseCode() + " - " + syllabus.getCourseName();
        String aaLink = "/syllabus/" + syllabus.getId();
        try {
            userRepository.findByRole(Role.ACADEMIC_AFFAIRS).forEach(aa -> {
                Notification notif = new Notification();
                notif.setRecipient(aa);
                notif.setMessage(aaMessage);
                notif.setSyllabusId(syllabus.getId());
                notif.setLink(aaLink);
                notificationRepository.save(notif);
            });
        } catch (Exception e) {
            System.err.println("[AA Notification] failed: " + e.getMessage());
        }

        return SyllabusResponse.from(syllabus);
    }

    public SyllabusResponse aaApprove(Long id, String comments, String email) {
        User user = getUser(email);
        Syllabus syllabus = getSyllabus(id);

        if (syllabus.getStatus() != SyllabusStatus.PENDING_APPROVAL) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only PENDING_APPROVAL syllabi can be AA-approved");
        }
        syllabus.setStatus(SyllabusStatus.APPROVED);
        syllabusRepository.save(syllabus);
        logHistory(syllabus, user, "AA_APPROVED", comments);
        notifyFollowers(syllabus, "Syllabus " + syllabus.getCourseCode() + " - " + syllabus.getCourseName() + " has been fully approved");

        if (syllabus.getCreatedBy() != null) {
            try {
                Notification notif = new Notification();
                notif.setRecipient(syllabus.getCreatedBy());
                notif.setMessage("Your syllabus " + syllabus.getCourseCode() + " has been fully approved");
                notif.setSyllabusId(syllabus.getId());
                notif.setLink("/syllabus/" + syllabus.getId());
                notificationRepository.save(notif);
            } catch (Exception e) {
                System.err.println("[AA Approve Notification] failed: " + e.getMessage());
            }
        }

        return SyllabusResponse.from(syllabus);
    }

    public SyllabusResponse aaReject(Long id, String reason, String email) {
        User user = getUser(email);
        Syllabus syllabus = getSyllabus(id);

        if (syllabus.getStatus() != SyllabusStatus.PENDING_APPROVAL) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only PENDING_APPROVAL syllabi can be AA-rejected");
        }
        if (reason == null || reason.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rejection reason is required");
        }
        syllabus.setStatus(SyllabusStatus.AA_REJECTED);
        syllabus.setRejectionReason(reason);
        syllabusRepository.save(syllabus);
        logHistory(syllabus, user, "AA_REJECTED", reason);

        if (syllabus.getCreatedBy() != null) {
            try {
                Notification notif = new Notification();
                notif.setRecipient(syllabus.getCreatedBy());
                notif.setMessage("Your syllabus " + syllabus.getCourseCode()
                        + " was rejected by Academic Affairs: " + reason);
                notif.setSyllabusId(syllabus.getId());
                notif.setLink("/syllabus/" + syllabus.getId());
                notificationRepository.save(notif);
            } catch (Exception e) {
                System.err.println("[AA Reject Notification] failed: " + e.getMessage());
            }
        }

        return SyllabusResponse.from(syllabus);
    }

    public SyllabusResponse reject(Long id, String reason, String email) {
        User user = getUser(email);
        Syllabus syllabus = getSyllabus(id);

        if (syllabus.getStatus() != SyllabusStatus.PENDING_REVIEW) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only PENDING_REVIEW syllabi can be rejected");
        }
        if (reason == null || reason.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rejection reason is required");
        }
        syllabus.setStatus(SyllabusStatus.REJECTED);
        syllabus.setRejectionReason(reason);
        syllabus.setReviewedBy(user);
        syllabusRepository.save(syllabus);
        logHistory(syllabus, user, "REJECTED", reason);
        notifyFollowers(syllabus, "Syllabus " + syllabus.getCourseCode() + " - " + syllabus.getCourseName() + " has been updated");
        return SyllabusResponse.from(syllabus);
    }

    public SyllabusResponse publish(Long id, String email) {
        User user = getUser(email);
        Syllabus syllabus = getSyllabus(id);

        if (syllabus.getStatus() != SyllabusStatus.APPROVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only APPROVED syllabi can be published");
        }
        syllabus.setStatus(SyllabusStatus.PUBLISHED);
        syllabusRepository.save(syllabus);
        logHistory(syllabus, user, "PUBLISHED", null);
        notifyFollowers(syllabus, "Syllabus " + syllabus.getCourseCode() + " - " + syllabus.getCourseName() + " has been updated");
        return SyllabusResponse.from(syllabus);
    }

    public SyllabusResponse newVersion(Long id, String email) {
        User user = getUser(email);
        Syllabus original = getSyllabus(id);

        if (!original.getCreatedBy().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this syllabus");
        }
        if (original.getStatus() != SyllabusStatus.APPROVED && original.getStatus() != SyllabusStatus.PUBLISHED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only APPROVED or PUBLISHED syllabi can spawn a new version");
        }

        Syllabus draft = Syllabus.builder()
                .courseCode(original.getCourseCode())
                .courseName(original.getCourseName())
                .department(original.getDepartment())
                .credits(original.getCredits())
                .academicYear(original.getAcademicYear())
                .semester(original.getSemester())
                .description(original.getDescription())
                .learningOutcomes(original.getLearningOutcomes())
                .assessmentMethods(original.getAssessmentMethods())
                .prerequisites(original.getPrerequisites())
                .materials(original.getMaterials())
                .ploOutcomes(original.getPloOutcomes())
                .cloMappings(original.getCloMappings())
                .versionNumber(original.getVersionNumber() + 1)
                .status(SyllabusStatus.DRAFT)
                .createdBy(user)
                .build();

        Syllabus saved = syllabusRepository.save(draft);
        logHistory(saved, user, "NEW_VERSION", "Based on v" + original.getVersionNumber());
        notifyFollowers(original, "Syllabus " + original.getCourseCode() + " - " + original.getCourseName() + " has a new version");
        saveSnapshot(original);
        return SyllabusResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> listMine(String email) {
        User user = getUser(email);
        return syllabusRepository.findByCreatedByOrderByUpdatedAtDesc(user)
                .stream().map(SyllabusResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> listPendingReview() {
        return syllabusRepository.findByStatusOrderByUpdatedAtDesc(SyllabusStatus.PENDING_REVIEW)
                .stream().map(SyllabusResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> listPendingApproval() {
        return syllabusRepository.findByStatusOrderByUpdatedAtDesc(SyllabusStatus.PENDING_APPROVAL)
                .stream().map(SyllabusResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> searchByDepartment(String keyword, String statusStr, String email) {
        User hod = getUser(email);
        String department = hod.getDepartment();
        SyllabusStatus status = parseStatus(statusStr);
        return syllabusRepository.searchByDepartment(department, keyword, status)
                .stream().map(SyllabusResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> searchAll(String keyword, String statusStr) {
        SyllabusStatus status = parseStatus(statusStr);
        return syllabusRepository.searchAll(keyword, status)
                .stream().map(SyllabusResponse::from).collect(Collectors.toList());
    }

    private SyllabusStatus parseStatus(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) return null;
        try { return SyllabusStatus.valueOf(statusStr); }
        catch (IllegalArgumentException e) { return null; }
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> listAll() {
        return syllabusRepository.findAll().stream()
                .map(SyllabusResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> listPublic(String keyword) {
        List<SyllabusStatus> statuses = List.of(SyllabusStatus.PUBLISHED);
        return syllabusRepository.searchPublic(keyword, statuses)
                .stream().map(SyllabusResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SyllabusResponse getById(Long id, String email) {
        Syllabus syllabus = getSyllabus(id);
        if (email != null) {
            User user = getUser(email);
            if (user.getRole() == Role.STUDENT) {
                if (syllabus.getStatus() != SyllabusStatus.PUBLISHED) {
                    throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
                }
            }
        }
        return SyllabusResponse.from(syllabus);
    }

    @Transactional(readOnly = true)
    public String aiSummary(Long id) {
        Syllabus s = getSyllabus(id);
        StringBuilder sb = new StringBuilder();
        sb.append("📚 TÓM TẮT MÔN HỌC\n\n");

        sb.append(nvl(s.getCourseName()))
          .append(" (").append(nvl(s.getCourseCode())).append(")")
          .append(" là môn học ").append(s.getCredits() != null ? s.getCredits() : "N/A").append(" tín chỉ");
        if (hasValue(s.getDepartment())) {
            sb.append(" thuộc khoa ").append(s.getDepartment());
        }
        sb.append(".\n");

        if (hasValue(s.getLearningOutcomes())) {
            sb.append("\n🎯 Mục tiêu môn học:\n").append(s.getLearningOutcomes()).append("\n");
        }
        if (hasValue(s.getAssessmentMethods())) {
            sb.append("\n📊 Hình thức đánh giá:\n").append(s.getAssessmentMethods()).append("\n");
        }
        if (hasValue(s.getDescription())) {
            sb.append("\n📖 Mô tả:\n").append(s.getDescription()).append("\n");
        }
        if (hasValue(s.getMaterials())) {
            sb.append("\n📚 Tài liệu tham khảo:\n").append(s.getMaterials()).append("\n");
        }

        String dept = hasValue(s.getDepartment()) ? s.getDepartment() : "chuyên ngành";
        sb.append("\n✅ Môn học này giúp sinh viên nắm vững kiến thức nền tảng và ứng dụng thực tiễn trong lĩnh vực ")
          .append(dept).append(".\n");
        sb.append("\n⚡ Powered by SMD AI (Demo)");

        return sb.toString();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listVersions(Long syllabusId) {
        Syllabus syllabus = getSyllabus(syllabusId);
        return versionRepository.findByCourseCodeOrderBySnapshotAtAsc(syllabus.getCourseCode())
                .stream()
                .map(v -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", v.getId());
                    m.put("versionNumber", v.getVersionNumber());
                    m.put("snapshotAt", v.getSnapshotAt());
                    m.put("courseCode", v.getCourseCode());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> aiCompare(Long syllabusId, Long versionId1, Long versionId2) {
        Syllabus syllabus = getSyllabus(syllabusId);

        SyllabusVersion v1 = versionRepository.findById(versionId1)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Version not found: " + versionId1));
        SyllabusVersion v2 = versionRepository.findById(versionId2)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Version not found: " + versionId2));

        if (!v1.getCourseCode().equals(syllabus.getCourseCode())
                || !v2.getCourseCode().equals(syllabus.getCourseCode())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Both versions must belong to the same syllabus");
        }

        String prompt = "So sánh 2 phiên bản giáo trình sau và chỉ ra những thay đổi chính:\n\n"
                + "PHIÊN BẢN CŨ (v" + v1.getVersionNumber() + "):\n"
                + formatSnapshot(v1.getContentJson()) + "\n\n"
                + "PHIÊN BẢN MỚI (v" + v2.getVersionNumber() + "):\n"
                + formatSnapshot(v2.getContentJson()) + "\n\n"
                + "Hãy liệt kê các thay đổi theo từng mục: Mô tả, CLO, Phương pháp đánh giá, Tài liệu. "
                + "Nếu không có thay đổi ở mục nào thì ghi 'Không thay đổi'.";

        String comparison = geminiService.generateContent(prompt);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("comparison", comparison);
        result.put("version1Number", v1.getVersionNumber());
        result.put("version2Number", v2.getVersionNumber());
        return result;
    }

    private void saveSnapshot(Syllabus syllabus) {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("courseCode", nvl(syllabus.getCourseCode()));
            data.put("courseName", nvl(syllabus.getCourseName()));
            data.put("credits", syllabus.getCredits());
            data.put("department", nvl(syllabus.getDepartment()));
            data.put("academicYear", nvl(syllabus.getAcademicYear()));
            data.put("semester", nvl(syllabus.getSemester()));
            data.put("description", nvl(syllabus.getDescription()));
            data.put("learningOutcomes", nvl(syllabus.getLearningOutcomes()));
            data.put("assessmentMethods", nvl(syllabus.getAssessmentMethods()));
            data.put("materials", nvl(syllabus.getMaterials()));

            String contentJson = new ObjectMapper().writeValueAsString(data);

            SyllabusVersion version = new SyllabusVersion();
            version.setSyllabusId(syllabus.getId());
            version.setCourseCode(syllabus.getCourseCode());
            version.setVersionNumber(syllabus.getVersionNumber());
            version.setContentJson(contentJson);
            versionRepository.save(version);
        } catch (Exception e) {
            System.err.println("[SyllabusVersion] snapshot failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String formatSnapshot(String contentJson) {
        try {
            Map<String, Object> data = new ObjectMapper().readValue(contentJson, Map.class);
            return "Tên môn: " + data.getOrDefault("courseName", "N/A") + "\n"
                    + "Mã môn: " + data.getOrDefault("courseCode", "N/A") + "\n"
                    + "Số tín chỉ: " + data.getOrDefault("credits", "N/A") + "\n"
                    + "Mô tả: " + data.getOrDefault("description", "N/A") + "\n"
                    + "Chuẩn đầu ra (CLO): " + data.getOrDefault("learningOutcomes", "N/A") + "\n"
                    + "Phương pháp đánh giá: " + data.getOrDefault("assessmentMethods", "N/A") + "\n"
                    + "Tài liệu: " + data.getOrDefault("materials", "N/A");
        } catch (Exception e) {
            return contentJson;
        }
    }

    private String nvl(String s) {
        return s != null ? s : "Không có thông tin";
    }

    private boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }
}
