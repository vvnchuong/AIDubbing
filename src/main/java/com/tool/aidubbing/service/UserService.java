package com.tool.aidubbing.service;

import com.tool.aidubbing.dto.request.UserCreationRequest;
import com.tool.aidubbing.dto.response.UserResponse;
import com.tool.aidubbing.entity.User;
import com.tool.aidubbing.enums.ErrorCode;
import com.tool.aidubbing.exception.AppException;
import com.tool.aidubbing.mapper.UserMapper;
import com.tool.aidubbing.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;

    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        return userMapper
                .toUserResponse(userRepository.save(user));
    }

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

}
