package com.ypeoomes.Repositories;

import com.ypeoomes.Entities.RecurringSchedule;
import com.ypeoomes.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecurringScheduleRepository extends JpaRepository<RecurringSchedule, Long> {
    List<RecurringSchedule> findByUser(User user);
}
