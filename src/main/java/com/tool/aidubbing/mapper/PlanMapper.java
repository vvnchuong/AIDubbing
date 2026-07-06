package com.tool.aidubbing.mapper;

import com.tool.aidubbing.dto.request.PlanRequest;
import com.tool.aidubbing.dto.response.PlanResponse;
import com.tool.aidubbing.entity.Plan;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "Spring")
public interface PlanMapper {

    Plan toPlan(PlanRequest request);

    PlanResponse toPlanResponse(Plan plan);

    void updatePlan(@MappingTarget Plan plan, PlanRequest request);

}
