package com.tool.aidubbing.controller;

import com.tool.aidubbing.dto.PlanRequest;
import com.tool.aidubbing.dto.response.ApiResponse;
import com.tool.aidubbing.dto.response.PlanResponse;
import com.tool.aidubbing.service.PlanService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlanController {

    PlanService planService;

    @PostMapping
    public ApiResponse<PlanResponse> createPlan(
            @RequestBody PlanRequest request) {
        return ApiResponse.<PlanResponse>builder()
                .message("Plan created successfully.")
                .result(planService.createPlan(request))
                .build();
    }

    @GetMapping("/{planId}")
    public ApiResponse<PlanResponse> getPlan(
            @PathVariable("planId") long planId) {
        return ApiResponse.<PlanResponse>builder()
                .message("Plan retrieved successfully.")
                .result(planService.getPlan(planId))
                .build();
    }

    @GetMapping
    public ApiResponse<List<PlanResponse>> getAllPlans() {
        return ApiResponse.<List<PlanResponse>>builder()
                .message("Plans retrieved successfully.")
                .result(planService.getAllPlans())
                .build();
    }

    @PutMapping("/{planId}")
    public ApiResponse<Void> updatePlan(
            @PathVariable("planId") long planId,
            @RequestBody PlanRequest request) {
        planService.updatePlan(planId, request);
        return ApiResponse.<Void>builder()
                .message("Plan updated successfully.")
                .build();
    }

    @DeleteMapping("/{planId}")
    public ApiResponse<Void> deletePlan(
            @PathVariable("planId") long planId) {
        planService.deletePlan(planId);
        return ApiResponse.<Void>builder()
                .message("Plan deleted successfully.")
                .build();
    }

}
