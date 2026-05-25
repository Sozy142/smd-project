package com.smd.repository;

import com.smd.entity.SyllabusFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SyllabusFollowRepository extends JpaRepository<SyllabusFollow, Long> {

    boolean existsByStudentIdAndSyllabusId(Long studentId, Long syllabusId);

    List<SyllabusFollow> findByStudentId(Long studentId);

    @Query("SELECT f FROM SyllabusFollow f JOIN FETCH f.student WHERE f.syllabus.id = :syllabusId")
    List<SyllabusFollow> findBySyllabusId(@Param("syllabusId") Long syllabusId);

    long countBySyllabusId(Long syllabusId);

    @Transactional
    @Modifying
    @Query("DELETE FROM SyllabusFollow f WHERE f.student.id = :studentId AND f.syllabus.id = :syllabusId")
    void deleteByStudentIdAndSyllabusId(@Param("studentId") Long studentId,
                                        @Param("syllabusId") Long syllabusId);
}
