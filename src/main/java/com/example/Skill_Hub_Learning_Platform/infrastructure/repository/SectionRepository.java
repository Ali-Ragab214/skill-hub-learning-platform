package com.example.Skill_Hub_Learning_Platform.infrastructure.repository;

import com.example.Skill_Hub_Learning_Platform.domain.models.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section,Long> {
    List<Section> findByCourseIdOrderByOrderIndexAsc(Long courseId);
    void deleteByCourseId(Long courseId);
    int countByCourseId(Long courseId);
}
