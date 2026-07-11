package com.tool.aidubbing.service;

import com.tool.aidubbing.dto.PlanRequest;
import com.tool.aidubbing.dto.response.PlanResponse;
import com.tool.aidubbing.entity.Plan;
import com.tool.aidubbing.enums.ErrorCode;
import com.tool.aidubbing.exception.AppException;
import com.tool.aidubbing.mapper.PlanMapper;
import com.tool.aidubbing.repository.PlanRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlanService {

    PlanRepository planRepository;
    PlanMapper planMapper;

    public PlanResponse createPlan(PlanRequest request) {
        if (planRepository.existsByName(request.getName()))
            throw new AppException(ErrorCode.PLAN_HAS_EXISTED);

        Plan plan = planMapper.toPlan(request);
        plan = planRepository.save(plan);

        return planMapper.toPlanResponse(plan);
    }

    public PlanResponse getPlan(long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        return planMapper.toPlanResponse(plan);
    }

    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .map(planMapper::toPlanResponse)
                .toList();
    }

    public void updatePlan(long planId, PlanRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        planMapper.updatePlan(plan, request);
    }

    public void deletePlan(long planId) {
        if (!planRepository.existsById(planId))
            throw new AppException(ErrorCode.PLAN_NOT_FOUND);

        planRepository.deleteById(planId);
    }

}
