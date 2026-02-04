package com.erp.mini.util;

import com.erp.mini.common.security.CustomUserDetails;
import com.erp.mini.user.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

public class TestLoginUser {

    public static Authentication setAuthLogin(User user) {
        var principal = new CustomUserDetails(
                user.getId(),
                user.getEmployeeNumber(),
                user.getPassword(),
                true
        );

        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of()
        );
    }

}
