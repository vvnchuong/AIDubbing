package com.tool.aidubbing.dto.response;

import com.tool.aidubbing.enums.TransactionStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionResponse {

    Long userId;
    Integer amount;
    String code;
    Long planId;
    TransactionStatus status;
    String provider;
    Instant createdAt;

}
