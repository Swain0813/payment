package com.payment.trade.dao;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface ClearingMapper {

    /**
     * 根据key查询value
     *
     * @param key
     * @return value
     */
    @Select("select value from tcs_sys_const where `key` = #{key}")
    String selectUrlByKey(String key);

}
