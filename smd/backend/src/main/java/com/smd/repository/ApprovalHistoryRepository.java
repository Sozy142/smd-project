package com.smd.repository;

import com.smd.entity.ApprovalHistory;
import com.smd.entity.Syllabus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {
    List<ApprovalHistory> findBySyllabusOrderByActionDateDesc(Syllabus syllabus);
}
