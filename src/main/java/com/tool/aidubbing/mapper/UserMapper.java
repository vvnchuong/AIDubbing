package com.tool.aidubbing.mapper;

import com.tool.aidubbing.dto.request.UserCreationRequest;
import com.tool.aidubbing.dto.response.UserResponse;
import com.tool.aidubbing.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "Spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

}
