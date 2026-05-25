package com.smd.repository;

import com.smd.entity.SyllabusFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SyllabusFeedbackRepository extends JpaRepository<SyllabusFeedback, Long> {

    @Query("SELECT f FROM SyllabusFeedback f JOIN FETCH f.student WHERE f.syllabus.id = :syllabusId ORDER BY f.createdAt DESC")
    List<SyllabusFeedback> findBySyllabusIdOrderByCreatedAtDesc(@Param("syllabusId") Long syllabusId);
}
