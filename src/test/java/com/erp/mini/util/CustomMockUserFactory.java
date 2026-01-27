package com.erp.mini.util;

import com.erp.mini.common.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class CustomMockUserFactory implements WithSecurityContextFactory<CustomMockUser> {

    @Override
    public SecurityContext createSecurityContext(CustomMockUser annotation) {
        Long userId = annotation.id();
        String employeeNumber = annotation.employeeNumber();

        CustomUserDetails customUserDetails = new CustomUserDetails(userId, employeeNumber, "encodedPassword", true);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);

        return context;
    }
}
