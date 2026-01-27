package com.erp.mini.user.service;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.user.domain.User;
import com.erp.mini.user.domain.UserFixture;
import com.erp.mini.user.dto.AddUserRequest;
import com.erp.mini.user.dto.ResetPasswordRequest;
import com.erp.mini.user.dto.UpdatePasswordRequest;
import com.erp.mini.user.repo.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Nested
    class add_user_test {
        @Test
        void add_user_success() {
            String employeeNumber = "emp-001";
            String savedPassword = "encodedPassword";

            AddUserRequest request = new AddUserRequest(
                    "tester", employeeNumber, "rawPassword"
            );

            given(userRepository.existsByEmployeeNumber(employeeNumber)).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn(savedPassword);

            userService.addUser(request);

            then(userRepository).should().existsByEmployeeNumber(employeeNumber);
            then(userRepository).should().save(argThat(user ->
                    user.getName().equals(request.name())
                            && user.getPassword().equals(savedPassword)
                            && user.getEmployeeNumber().equals(employeeNumber)
            ));
        }

        @Test
        void add_user_fail_with_exists_employee_number() {
            String employeeNumber = "emp-001";

            AddUserRequest request = new AddUserRequest(
                    "tester", employeeNumber, "rawPassword"
            );

            given(userRepository.existsByEmployeeNumber(employeeNumber)).willReturn(true);

            assertThatThrownBy(() -> userService.addUser(request))
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;

                        assertThat(be.getErrorCode().getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }
    }

    @Nested
    class reset_password_test {
        @Test
        void reset_password_success() {
            User user = UserFixture.create();
            ResetPasswordRequest request = new ResetPasswordRequest(user.getName(), user.getEmployeeNumber());

            given(userRepository.findByEmployeeNumber(request.employeeNumber())).willReturn(Optional.of(user));
            given(passwordEncoder.encode(request.employeeNumber() + "1234")).willReturn("newEncodedPassword");

            userService.resetPassword(request);

            then(userRepository).should().findByEmployeeNumber(user.getEmployeeNumber());
            assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        }

        @Test
        void reset_password_fail_with_not_found_employee_number() {
            User user = UserFixture.create();
            ResetPasswordRequest request = new ResetPasswordRequest(user.getName(), "MEP15151515");

            given(userRepository.findByEmployeeNumber(request.employeeNumber())).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.resetPassword(request))
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        void reset_password_fail_with_not_match_name() {
            User user = UserFixture.create();
            ResetPasswordRequest request = new ResetPasswordRequest("ester1234", user.getEmployeeNumber());

            given(userRepository.findByEmployeeNumber(request.employeeNumber())).willReturn(Optional.of(user));
            assertThatThrownBy(() -> userService.resetPassword(request))
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class update_password_test {
        @Test
        void update_password_success() {
            User user = UserFixture.create();
            UpdatePasswordRequest request = new UpdatePasswordRequest("currentPassword", "newPassword");

            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.currentPassword(), user.getPassword())).willReturn(true);
            given(passwordEncoder.encode(request.newPassword())).willReturn("newEncodedPassword");

            userService.updatePassword(user.getId(), request);

            then(userRepository).should().findById(user.getId());
            assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        }

        @Test
        void update_password_fail_with_not_match_current_password() {
            User user = UserFixture.create();
            UpdatePasswordRequest request = new UpdatePasswordRequest("currentPassword", "newPassword");

            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.currentPassword(), user.getPassword())).willReturn(false);

            assertThatThrownBy(() -> userService.updatePassword(user.getId(), request))
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }

        @Test
        void update_password_fail_with_not_found_user_id() {
            User user = UserFixture.create();
            UpdatePasswordRequest request = new UpdatePasswordRequest("currentPassword", "newPassword");

            given(userRepository.findById(user.getId())).willReturn(Optional.empty());
            assertThatThrownBy(() -> userService.updatePassword(user.getId(), request))
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }
}