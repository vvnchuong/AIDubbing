package com.tool.aidubbing.controller;

import com.tool.aidubbing.dto.response.ApiResponse;
import com.tool.aidubbing.dto.response.UserResponse;
import com.tool.aidubbing.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    /**
     * Test JwtAuthFilter: gọi kèm header "Authorization: Bearer <token>".
     * userId lấy ra từ SecurityContext - chính là cái JwtAuthFilter set vào
     * lúc verify token thành công (principal = userId, xem lại JwtAuthFilter).
     */
    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser() {
        Long userId = (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return ApiResponse.<UserResponse>builder()
                .message("OK")
                .result(userService.getUserById(userId))
                .build();
    }
}