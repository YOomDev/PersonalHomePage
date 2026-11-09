package com.ypeoomes.Repositories;

import com.ypeoomes.Entities.ShiftTemplate;
import com.ypeoomes.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
    List<ShiftTemplate> findByUser(User user);
}
