package com.ypeoomes.Repositories;

import com.ypeoomes.Entities.CalendarEvent;
import com.ypeoomes.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByUserOrderByStartAtAsc(User user);
}