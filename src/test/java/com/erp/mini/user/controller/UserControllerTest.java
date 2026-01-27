package com.erp.mini.user.controller;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.user.dto.AddUserRequest;
import com.erp.mini.user.dto.ResetPasswordRequest;
import com.erp.mini.user.dto.UpdatePasswordRequest;
import com.erp.mini.user.service.UserService;
import com.erp.mini.util.CustomMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

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
                    .andDo(print());
        }

        @Test
        void add_user_fail_with_missing_employee_number() throws Exception {
            AddUserRequest request = new AddUserRequest(
                    "송기성", null, "rawPassword"
            );

            mockMvc.perform(post("/api/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void add_user_fail_with_duplicate_employee_number() throws Exception {
            AddUserRequest request = new AddUserRequest(
                    "송기성", "EMP001", "rawPassword"
            );

            doThrow(new BusinessException(ErrorCode.CONFLICT)).when(userService).addUser(any(AddUserRequest.class));

            mockMvc.perform(post("/api/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }
    }

    @Nested
    class reset_password_test {
        @Test
        void reset_password_success() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest(
                    "송기성", "EMP001"
            );

            mockMvc.perform(patch("/api/user/password-reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void reset_password_fail_with_missing_name() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest(
                    null, "EMP001"
            );

            mockMvc.perform(patch("/api/user/password-reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void reset_password_fail_with_not_found_user() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest(
                    "송기성", "EMP001"
            );

            doThrow(new BusinessException(ErrorCode.NOT_FOUND)).when(userService).resetPassword(any(ResetPasswordRequest.class));

            mockMvc.perform(patch("/api/user/password-reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class update_password_test {
        @Test
        void update_password_success() throws Exception {
            UpdatePasswordRequest request = new UpdatePasswordRequest(
                    "rawPassword", "newPassword"
            );

            mockMvc.perform(patch("/api/user/password-update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void update_password_fail_with_missing_current_password() throws Exception {
            UpdatePasswordRequest request = new UpdatePasswordRequest(
                    null, "newPassword"
            );

            mockMvc.perform(patch("/api/user/password-update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void update_password_fail_with_not_found_user() throws Exception {
            UpdatePasswordRequest request = new UpdatePasswordRequest(
                    "currentPassword", "newPassword"
            );

            doThrow(new BusinessException(ErrorCode.NOT_FOUND))
                    .when(userService).updatePassword(anyLong(), any(UpdatePasswordRequest.class));

            mockMvc.perform(patch("/api/user/password-update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        void update_password_fail_with_not_match_current_password() throws Exception {
            UpdatePasswordRequest request = new UpdatePasswordRequest(
                    "currentPassword", "wrongPassword"
            );

            doThrow(new BusinessException(ErrorCode.INVALID_REQUEST))
                    .when(userService).updatePassword(anyLong(), any(UpdatePasswordRequest.class));

            mockMvc.perform(patch("/api/user/password-update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}