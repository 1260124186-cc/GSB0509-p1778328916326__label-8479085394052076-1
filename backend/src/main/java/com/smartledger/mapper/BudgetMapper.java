package com.smartledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartledger.entity.Budget;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BudgetMapper extends BaseMapper<Budget> {
}
