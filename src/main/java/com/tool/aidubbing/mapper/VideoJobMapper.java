package com.tool.aidubbing.mapper;

import com.tool.aidubbing.dto.request.VideoJobCreationRequest;
import com.tool.aidubbing.dto.response.VideoJobResponse;
import com.tool.aidubbing.entity.VideoJob;
import org.mapstruct.Mapper;

@Mapper(componentModel = "Spring")
public interface VideoJobMapper {

    VideoJob toVideoJob(VideoJobCreationRequest request);

    VideoJobResponse toVideoJobResponse(VideoJob videoJob);

}
