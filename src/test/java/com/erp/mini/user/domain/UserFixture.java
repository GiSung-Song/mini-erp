package com.erp.mini.user.domain;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class UserFixture {

    private static long sequence = 1L;

    public static User create() {
        return create("tester" + sequence, "EMP" + sequence);
    }

    public static User create(String name, String emp) {
        User user = User.createUser(name, emp, "encodedPassword");
        ReflectionTestUtils.setField(user, "id", sequence++);
        return user;
    }
}