package com.example.coditas.mini_e_commerce.repository;

import com.example.coditas.mini_e_commerce.entity.User;
import com.example.coditas.mini_e_commerce.enums.ActiveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String username);
    boolean existsByEmail(String email);

    Optional<User> findByUserIdAndIsActive(String userId, ActiveStatus activeStatus);

    Optional<User> findByUserId(String userId);

    Page<User> findAll(Specification<User> spec, Pageable pageable);
}
