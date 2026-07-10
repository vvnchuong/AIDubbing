package com.tool.aidubbing.service;

import com.tool.aidubbing.dto.response.PaymentQrResponse;
import com.tool.aidubbing.dto.response.TransactionResponse;
import com.tool.aidubbing.entity.Plan;
import com.tool.aidubbing.entity.Transaction;
import com.tool.aidubbing.enums.ErrorCode;
import com.tool.aidubbing.enums.TransactionStatus;
import com.tool.aidubbing.exception.AppException;
import com.tool.aidubbing.mapper.TransactionMapper;
import com.tool.aidubbing.repository.PlanRepository;
import com.tool.aidubbing.repository.TransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentService {

    final PlanRepository planRepository;
    final TransactionRepository transactionRepository;
    final UserService userService;
    final TransactionMapper transactionMapper;

    @Value("${app.sepay.bank-account}")
    String bankAccount;

    @Value("${app.sepay.bank-code}")
    String bankCode;

    @Value("${app.sepay.code-prefix}")
    String codePrefix;

    public PaymentQrResponse createPayment(Long userId, Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        String code = codePrefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .planId(planId)
                .amount(plan.getPrice())
                .code(code)
                .status(TransactionStatus.PENDING)
                .provider("SEPAY")
                .build();
        transaction = transactionRepository.save(transaction);

        String qrImageUrl = buildQrUrl(plan.getPrice(), code);

        return PaymentQrResponse.builder()
                .transactionId(transaction.getId())
                .code(code)
                .amount(plan.getPrice())
                .qrImageUrl(qrImageUrl)
                .build();
    }

    // user
    public List<TransactionResponse> getTransactionHistory(long userId) {
        return transactionRepository.findAllByUserId(userId)
                .stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();
    }

    // admin
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();
    }

    private String buildQrUrl(Integer amount, String code) {
        String description = UriUtils.encode(code, StandardCharsets.UTF_8);
        return "https://qr.sepay.vn/img?acc=" + bankAccount
                + "&bank=" + bankCode
                + "&amount=" + amount
                + "&des=" + description;
    }

    public void confirmPaymentManually(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (!"PENDING".equals(transaction.getStatus())) {
            throw new AppException(ErrorCode.TRANSACTION_ALREADY_PROCESSED);
        }

        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        userService.applyPlanPurchase(transaction.getUserId(), transaction.getPlanId());
    }
}