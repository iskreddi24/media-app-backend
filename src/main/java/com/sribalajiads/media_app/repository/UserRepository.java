package com.sribalajiads.media_app.repository;

import com.sribalajiads.media_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // A method to find a user by their username, which we'll need for login.
    Optional<User> findByUsername(String username);
}