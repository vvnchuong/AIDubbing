package com.tool.aidubbing.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

    Long id;
    String email;
    String name;
    String avatarUrl;
    Long planId;
    Integer quotaMinutesLeft;
    String role;
    Instant createdAt;

}
