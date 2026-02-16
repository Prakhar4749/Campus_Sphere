package com.authService.repositories;


import com.authService.entities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
    Optional<User> findByEmail(String email); // Use 'email' if that is the correct field for login

    boolean existsByEmail(String email);
}
