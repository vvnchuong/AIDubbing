package com.tool.aidubbing.controller;

import com.tool.aidubbing.dto.response.ApiResponse;
import com.tool.aidubbing.dto.response.PaymentQrResponse;
import com.tool.aidubbing.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentService paymentService;

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping("/plans/{planId}/create")
    public ApiResponse<PaymentQrResponse> createPayment(
            @PathVariable Long planId) {
        return ApiResponse.<PaymentQrResponse>builder()
                .message("Quét mã QR để thanh toán")
                .result(paymentService.createPayment(currentUserId(), planId))
                .build();
    }
}