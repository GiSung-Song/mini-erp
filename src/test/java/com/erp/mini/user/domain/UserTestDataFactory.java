package com.erp.mini.user.domain;

import com.erp.mini.user.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("integration")
public class UserTestDataFactory {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(String name, String employeeNumber) {
        User user =
                User.createUser(
                        name,
                        employeeNumber,
                        passwordEncoder.encode("rawPassword")
                );

        return userRepository.save(user);
    }
}