package com.mancy.quizplatform.repository;

import com.mancy.quizplatform.entity.AppUser;
import com.mancy.quizplatform.enums.Role;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    List<AppUser> findByRole(Role role);

    List<AppUser> findAllByOrderByCreatedAtDesc();

    List<AppUser> findByRoleOrderByCreatedAtDesc(Role role);

    // SELECT ... FOR UPDATE: a second request for the same user waits until the first one finishes.
    // Used so a double click on "Start quiz" cannot create two attempts.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from AppUser u where u.email = :email")
    Optional<AppUser> findByEmailForUpdate(@Param("email") String email);
}
