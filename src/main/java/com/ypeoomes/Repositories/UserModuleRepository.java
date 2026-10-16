package com.ypeoomes.Repositories;

import com.ypeoomes.Entities.User;
import com.ypeoomes.Entities.UserModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserModuleRepository extends JpaRepository<UserModule, Long> {
    List<UserModule> findByUserOrderByDisplayOrderAsc(User user);
    List<UserModule> findByUserAndEnabledTrueOrderByDisplayOrderAsc(User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserModule um WHERE um.user NOT IN (SELECT u FROM User u)")
    int deleteOrphanedModules();
}