package com.digi.auth.repository;

import com.digi.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Find by username
    @Query(value="select u from User u where u.username=?1")
    Optional<User> findByUsername(String username);

    @Query(value="select u from User u where u.email=?1")
    Optional<User> findByEmail(String email);
    @Query(value="select u from User u where u.role =?1")
    Optional<User> findByRole(String role);
    @Query(value="select u from User u where u.email=?1 and u.password=?2")
    Optional<User> findByEmailAndPassword(String email, String password);
}
