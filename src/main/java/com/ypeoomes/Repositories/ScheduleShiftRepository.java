package com.ypeoomes.Repositories;

import com.ypeoomes.Entities.ScheduleShift;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleShiftRepository extends JpaRepository<ScheduleShift, Long> {
    boolean existsByTemplateId(Long templateId);
}
