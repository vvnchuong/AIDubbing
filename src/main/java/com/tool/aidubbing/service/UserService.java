package com.tool.aidubbing.service;

import com.tool.aidubbing.dto.request.UserCreationRequest;
import com.tool.aidubbing.dto.response.UserResponse;
import com.tool.aidubbing.entity.Plan;
import com.tool.aidubbing.entity.Transaction;
import com.tool.aidubbing.entity.User;
import com.tool.aidubbing.enums.ErrorCode;
import com.tool.aidubbing.enums.TransactionStatus;
import com.tool.aidubbing.exception.AppException;
import com.tool.aidubbing.mapper.UserMapper;
import com.tool.aidubbing.repository.PlanRepository;
import com.tool.aidubbing.repository.TransactionRepository;
import com.tool.aidubbing.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PlanRepository planRepository;
    TransactionRepository transactionRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public UserResponse getUserById(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }

    public UserResponse applyPlanPurchase(long userId, long planId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        int currentQuota = user.getQuotaMinutesLeft() == null ? 0 : user.getQuotaMinutesLeft();
        user.setQuotaMinutesLeft(currentQuota + plan.getMonthlyMinutes());
        user.setQuotaResetAt(Instant.now().plus(30, ChronoUnit.DAYS));
        user.setPlanId(plan.getId());
        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    public UserResponse applyPlanPurchaseManually(long userId, long planId) {
        UserResponse response = applyPlanPurchase(userId, planId);

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setPlanId(planId);
        transaction.setAmount(plan.getPrice());
        transaction.setCode("MANUAL-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setProvider("MANUAL");
        transactionRepository.save(transaction);

        return response;
    }

}