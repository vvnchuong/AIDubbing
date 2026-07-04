package com.tool.aidubbing.service;

import com.tool.aidubbing.dto.request.VideoJobCreationRequest;
import com.tool.aidubbing.dto.response.VideoJobResponse;
import com.tool.aidubbing.entity.User;
import com.tool.aidubbing.entity.VideoJob;
import com.tool.aidubbing.enums.ErrorCode;
import com.tool.aidubbing.exception.AppException;
import com.tool.aidubbing.mapper.VideoJobMapper;
import com.tool.aidubbing.repository.UserRepository;
import com.tool.aidubbing.repository.VideoJobRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoJobService {

    VideoJobRepository videoJobRepository;
    UserRepository userRepository;
    VideoJobMapper videoJobMapper;
    FfprobeService ffprobeService;

    public VideoJobResponse createJob(long userId, VideoJobCreationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        double videoDurationMinutes = ffprobeService.getVideoDurationMinutes(request.getInputPath());

        if (videoDurationMinutes > user.getQuotaMinutesLeft())
            throw new AppException(ErrorCode.QUOTA_EXCEEDED);

        VideoJob videoJob = videoJobMapper.toVideoJob(request);
        videoJob.setUserId(user.getId());

        return videoJobMapper
                .toVideoJobResponse(videoJobRepository.save(videoJob));
    }

    public VideoJobResponse getJobById(long userId, long jobId) {
        VideoJob videoJob = videoJobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        if (!videoJob.getUserId().equals(userId))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return videoJobMapper.toVideoJobResponse(videoJob);
    }

    public List<VideoJobResponse> getMyJobs(long userId) {
        return videoJobRepository.findByUserId(userId)
                .stream()
                .map(videoJobMapper::toVideoJobResponse)
                .toList();
    }

}
