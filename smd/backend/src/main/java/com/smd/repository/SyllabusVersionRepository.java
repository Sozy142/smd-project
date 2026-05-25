package com.smd.repository;

import com.smd.entity.SyllabusVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyllabusVersionRepository extends JpaRepository<SyllabusVersion, Long> {

    List<SyllabusVersion> findByCourseCodeOrderBySnapshotAtAsc(String courseCode);
}
