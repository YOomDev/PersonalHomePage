package com.ypeoomes.Repositories;

import com.ypeoomes.Entities.CalendarShare;
import com.ypeoomes.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalendarShareRepository extends JpaRepository<CalendarShare, Long> {
    List<CalendarShare> findByOwner(User owner);
    List<CalendarShare> findByGranteeUsername(String granteeUsername);
    Optional<CalendarShare> findByOwnerAndGranteeUsername(User owner, String granteeUsername);
}
