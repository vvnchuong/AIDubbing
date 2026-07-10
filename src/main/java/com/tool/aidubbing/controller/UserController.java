package com.tool.aidubbing.controller;

import com.tool.aidubbing.dto.response.ApiResponse;
import com.tool.aidubbing.dto.response.UserResponse;
import com.tool.aidubbing.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser() {
        return ApiResponse.<UserResponse>builder()
                .message("OK")
                .result(userService.getUserById(currentUserId()))
                .build();
    }

    /**
     * "Mua gói" tạm thời - set chạy, chưa có cổng thanh toán thật.
     * Sau này thêm thanh toán, chỗ này sẽ được gọi từ webhook thanh toán, không phải gọi trực tiếp từ FE nữa.
     */
    @PostMapping("/plans/{planId}/purchase")
    public ApiResponse<UserResponse> purchasePlan(@PathVariable long planId) {
        return ApiResponse.<UserResponse>builder()
                .message("Mua gói thành công (test tay, chưa qua thanh toán thật)")
                .result(userService.applyPlanPurchaseManually(currentUserId(), planId))
                .build();
    }
}