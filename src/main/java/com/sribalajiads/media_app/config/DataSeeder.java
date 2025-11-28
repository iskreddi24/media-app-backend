package com.sribalajiads.media_app.config;

import com.sribalajiads.media_app.model.User;
import com.sribalajiads.media_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
    }

    private void seedUsers() {

        String adminUsername = "outspaceplanner"; // Or whatever username you want

        // Check if the user with the specified username already exists.
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername(adminUsername);

            // Set the password for this user
            adminUser.setPassword(passwordEncoder.encode("15212019"));

            userRepository.save(adminUser);
            System.out.println(">>> Created admin user with username: " + adminUsername);
        } else {
            System.out.println(">>> Admin user '" + adminUsername + "' already exists. Skipping seeding.");
        }
    }
}