package com.smd.repository;

import com.smd.entity.Syllabus;
import com.smd.entity.SyllabusStatus;
import com.smd.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SyllabusRepository extends JpaRepository<Syllabus, Long> {

    List<Syllabus> findByCreatedByOrderByUpdatedAtDesc(User createdBy);

    List<Syllabus> findByStatusOrderByUpdatedAtDesc(SyllabusStatus status);

    List<Syllabus> findByStatusInOrderByUpdatedAtDesc(List<SyllabusStatus> statuses);

    @Query("SELECT s FROM Syllabus s WHERE s.status IN :statuses AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(s.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.courseName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.department) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY s.updatedAt DESC")
    List<Syllabus> searchPublic(@Param("keyword") String keyword,
                                @Param("statuses") List<SyllabusStatus> statuses);

    List<Syllabus> findByCourseCodeAndStatusIn(String courseCode, List<SyllabusStatus> statuses);

    @Query("SELECT s FROM Syllabus s WHERE s.status = :status AND " +
           "LOWER(s.prerequisites) LIKE LOWER(CONCAT('%', :courseCode, '%'))")
    List<Syllabus> findByPrerequisitesContainingAndStatus(@Param("courseCode") String courseCode,
                                                          @Param("status") SyllabusStatus status);

    @Query("SELECT s FROM Syllabus s WHERE s.department = :department AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(s.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR s.status = :status) " +
           "ORDER BY s.updatedAt DESC")
    List<Syllabus> searchByDepartment(@Param("department") String department,
                                       @Param("keyword") String keyword,
                                       @Param("status") SyllabusStatus status);

    @Query("SELECT s FROM Syllabus s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(s.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR s.status = :status) " +
           "ORDER BY s.updatedAt DESC")
    List<Syllabus> searchAll(@Param("keyword") String keyword,
                              @Param("status") SyllabusStatus status);
}
