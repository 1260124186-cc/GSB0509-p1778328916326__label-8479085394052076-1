package com.smartledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartledger.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM transaction WHERE user_id = #{userId} AND book_id = #{bookId} AND type = #{type} AND transaction_date BETWEEN #{startDate} AND #{endDate}")
    BigDecimal sumAmountByDateRange(@Param("userId") Long userId, @Param("bookId") Long bookId, @Param("type") Integer type, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT c.id as categoryId, c.name as categoryName, c.icon as categoryIcon, COALESCE(SUM(t.amount), 0) as amount FROM transaction t JOIN category c ON t.category_id = c.id WHERE t.user_id = #{userId} AND t.book_id = #{bookId} AND t.type = #{type} AND t.transaction_date BETWEEN #{startDate} AND #{endDate} GROUP BY c.id, c.name, c.icon ORDER BY amount DESC")
    List<Map<String, Object>> sumByCategory(@Param("userId") Long userId, @Param("bookId") Long bookId, @Param("type") Integer type, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
