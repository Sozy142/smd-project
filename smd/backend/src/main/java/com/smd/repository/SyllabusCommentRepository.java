package com.smd.repository;

import com.smd.entity.Syllabus;
import com.smd.entity.SyllabusComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyllabusCommentRepository extends JpaRepository<SyllabusComment, Long> {
    List<SyllabusComment> findBySyllabusOrderByCreatedAtDesc(Syllabus syllabus);
    long countBySyllabus(Syllabus syllabus);
}
