package com.erp.mini.seed;

import com.erp.mini.common.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 시드 데이터 삽입 시 CreatedBy, UpdatedBy 값이 필요하므로 구현
 */
public final class SecurityContextUtil {

    private SecurityContextUtil() {}

    public static void runAs(CustomUserDetails principal, Runnable runnable) {
        SecurityContext original = SecurityContextHolder.getContext();

        try {
            var auth = new UsernamePasswordAuthenticationToken(
                    principal,
            null,
                    principal.getAuthorities()
            );

            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            runnable.run();
        } finally {
            SecurityContextHolder.setContext(original);
        }
    }
}
