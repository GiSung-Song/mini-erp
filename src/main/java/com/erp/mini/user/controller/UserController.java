package com.erp.mini.user.controller;

import com.erp.mini.common.response.CustomResponse;
import com.erp.mini.common.security.CustomUserDetails;
import com.erp.mini.user.dto.AddUserRequest;
import com.erp.mini.user.dto.ResetPasswordRequest;
import com.erp.mini.user.dto.UpdatePasswordRequest;
import com.erp.mini.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "User", description = "사용자 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 생성", description = "사용자를 등록한다.", security = @SecurityRequirement(name = ""))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "409", description = "사번 중복")
    })
    @PostMapping
    public ResponseEntity<CustomResponse<Void>> addUser(@Valid @RequestBody AddUserRequest request) {
        userService.addUser(request);

        return CustomResponse.created();
    }

    @Operation(summary = "비밀번호 초기화", description = "비밀번호를 초기화한다.", security = @SecurityRequirement(name = ""))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "초기화 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PatchMapping("/password-reset")
    public ResponseEntity<CustomResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);

        return CustomResponse.ok();
    }

    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PatchMapping("/password-update")
    public ResponseEntity<CustomResponse<Void>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        userService.updatePassword(userDetails.getUserId(), request);

        return CustomResponse.ok();
    }
}