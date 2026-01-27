package com.erp.mini.common.security;

import com.erp.mini.user.domain.User;
import com.erp.mini.user.domain.UserStatus;
import com.erp.mini.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmployeeNumber(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found: " + username));

        return new CustomUserDetails(
                user.getId(),
                user.getEmployeeNumber(),
                user.getPassword(),
                user.getStatus() == UserStatus.ACTIVE
        );
    }
}