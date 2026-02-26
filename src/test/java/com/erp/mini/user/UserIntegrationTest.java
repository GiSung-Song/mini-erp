package com.erp.mini.user;

import com.erp.mini.common.security.CustomUserDetails;
import com.erp.mini.user.domain.User;
import com.erp.mini.user.domain.UserTestDataFactory;
import com.erp.mini.user.dto.AddUserRequest;
import com.erp.mini.user.dto.ResetPasswordRequest;
import com.erp.mini.user.dto.UpdatePasswordRequest;
import com.erp.mini.user.repo.UserRepository;
import com.erp.mini.util.IntegrationTest;
import com.erp.mini.util.TestContainerManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserTestDataFactory factory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @Nested
    class add_user_test {
        @Test
        void add_user_success() throws Exception {
            AddUserRequest request = new AddUserRequest(
                    "송기성", "EMP001", "rawPassword"
            );

            mockMvc.perform(post("/api/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist())
                    .andDo(print());

            User findUser = userRepository.findByEmployeeNumber(request.employeeNumber())
                    .orElseThrow();

            assertThat(findUser.getName()).isEqualTo(request.name());
            assertThat(passwordEncoder.matches(request.password(), findUser.getPassword())).isTrue();
        }

        @Test
        void add_user_fail_with_duplicate_employee_number() throws Exception {
            AddUserRequest request = new AddUserRequest(
                    "SYSTEM", "SYSTEM", "rawPassword"
            );

            mockMvc.perform(post("/api/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error.code").value("CONFLICT"))
                    .andExpect(jsonPath("$.error.message").exists())
                    .andDo(print());
        }
    }

    @Nested
    class reset_password_test {
        @Test
        void reset_password_success() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest(
                    "SYSTEM", "SYSTEM"
            );

            mockMvc.perform(patch("/api/user/password-reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist())
                    .andDo(print());

            User findUser = userRepository.findByEmployeeNumber(request.employeeNumber())
                    .orElseThrow();

            assertThat(passwordEncoder.matches(request.employeeNumber() + "1234", findUser.getPassword())).isTrue();
        }

        @Test
        void reset_password_fail_with_not_found_user() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest(
                    "TESTER", "EMP001"
            );

            mockMvc.perform(patch("/api/user/password-reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"))
                    .andExpect(jsonPath("$.error.message").exists())
                    .andDo(print());
        }

        @Test
        void reset_password_fail_with_miss_match_name() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest(
                    "TESTER", "SYSTEM"
            );

            mockMvc.perform(patch("/api/user/password-reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"))
                    .andExpect(jsonPath("$.error.message").exists())
                    .andDo(print());
        }
    }

    @Nested
    class update_password_test {
        private User user;

        @BeforeEach
        void setUp() {
            user = factory.createUser("tester", "EMP001");
        }

        @Test
        void update_password_success() throws Exception {
            var principal = new CustomUserDetails(
                    user.getId(),
                    user.getEmployeeNumber(),
                    user.getPassword(),
                    true
            );

            var auth = new UsernamePasswordAuthenticationToken(
                    principal, null, List.of()
            );

            UpdatePasswordRequest request = new UpdatePasswordRequest("rawPassword", "newPassword");

            mockMvc.perform(patch("/api/user/password-update")
                            .with(authentication(auth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist())
                    .andDo(print());

            User findUser = userRepository.findByEmployeeNumber(user.getEmployeeNumber())
                    .orElseThrow();

            assertThat(passwordEncoder.matches(request.newPassword(), findUser.getPassword())).isTrue();
        }

        @Test
        void update_password_fail_with_miss_match_current_password() throws Exception {
            var principal = new CustomUserDetails(
                    user.getId(),
                    user.getEmployeeNumber(),
                    user.getPassword(),
                    true
            );

            var auth = new UsernamePasswordAuthenticationToken(
                    principal, null, List.of()
            );

            UpdatePasswordRequest request = new UpdatePasswordRequest("wrongPassword", "newPassword");

            mockMvc.perform(patch("/api/user/password-update")
                            .with(authentication(auth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.error.message").exists())
                    .andDo(print());
        }
    }
}
