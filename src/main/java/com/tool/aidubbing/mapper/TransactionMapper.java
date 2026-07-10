package com.tool.aidubbing.mapper;

import com.tool.aidubbing.dto.response.TransactionResponse;
import com.tool.aidubbing.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionResponse toTransactionResponse(Transaction transaction);

}
