package com.tool.aidubbing.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentQrResponse {

    Long transactionId;
    String code;
    Integer amount;
    String qrImageUrl;

}