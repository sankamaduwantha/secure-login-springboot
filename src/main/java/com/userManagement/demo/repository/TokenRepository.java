package com.userManagement.demo.repository;

import com.userManagement.demo.model.Token;
import com.userManagement.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByEmailAndUsedAtIsNullOrderByCreatedAtDesc(String email);

    void deleteByUser(User user);
}