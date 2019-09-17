package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Holidays;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface HolidaysMapper extends BaseMapper<Holidays> {

    /**
     * 根据日期与国家查询节假日信息
     *
     * @param date    日期
     * @param country 国家
     * @return 汇率实体
     */
    @Select("select id,country,date,name,create_time,update_time,creator,modifier,remark,enabled from holidays where DATE_FORMAT(date,'%Y-%m-%d') = #{date} and country = #{country} and enabled = 1")
    Holidays selectByDateAndCountry(@Param("date") String date, @Param("country") String country);
}
