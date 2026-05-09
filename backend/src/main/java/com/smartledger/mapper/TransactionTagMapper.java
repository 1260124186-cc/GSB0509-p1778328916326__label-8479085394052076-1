package com.smartledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartledger.entity.TransactionTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import java.util.List;

@Mapper
public interface TransactionTagMapper extends BaseMapper<TransactionTag> {

    @Delete("DELETE FROM transaction_tag WHERE transaction_id = #{transactionId}")
    int deleteByTransactionId(@Param("transactionId") Long transactionId);

    @Delete("DELETE FROM transaction_tag WHERE transaction_id IN (#{transactionIds})")
    int deleteByTransactionIds(@Param("transactionIds") List<Long> transactionIds);
}
