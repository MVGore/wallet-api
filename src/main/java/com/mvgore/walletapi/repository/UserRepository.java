package com.mvgore.walletapi.repository;

import com.mvgore.walletapi.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.management.relation.Role;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User findByUsername(String username);

    Optional<User> findByEmail(String email);

    long countByRole(String role);

    Page<User> findByRole(String role, Pageable pageable);

}