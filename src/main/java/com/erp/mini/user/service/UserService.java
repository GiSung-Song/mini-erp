package com.erp.mini.user.service;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.user.domain.User;
import com.erp.mini.user.dto.AddUserRequest;
import com.erp.mini.user.dto.ResetPasswordRequest;
import com.erp.mini.user.dto.UpdatePasswordRequest;
import com.erp.mini.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String RESET_SUFFIX = "1234";

    // 사용자 등록
    @Transactional
    public void addUser(AddUserRequest request) {
        if (userRepository.existsByEmployeeNumber(request.employeeNumber())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 존재하는 사번입니다.");
        }

        User user = User.createUser(
                request.name(),
                request.employeeNumber(),
                passwordEncoder.encode(request.password())
        );

        userRepository.save(user);
    }

    // 비밀번호 초기화
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmployeeNumber(request.employeeNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.getName().equals(request.name())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        user.updatePassword(passwordEncoder.encode(request.employeeNumber() + RESET_SUFFIX));
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "현재 비밀번호가 맞지 않습니다.");
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }
}