package com.example.coditas.mini_e_commerce.repository;

import com.example.coditas.mini_e_commerce.entity.Cart;
import com.example.coditas.mini_e_commerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
