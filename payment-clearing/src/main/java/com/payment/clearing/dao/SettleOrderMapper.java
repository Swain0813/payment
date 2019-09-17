package com.payment.clearing.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.SettleOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 机构结算交易Mapper
 */
@Repository
public interface SettleOrderMapper extends BaseMapper<SettleOrder> {

    @Select("select batch_no FROM settle_order where institution_code = #{institutionCode} and bank_code=#{bankCode} and settle_type=1 and trade_status=1 and date(create_time)=curdate() LIMIT 1")
    String getBatchNo(@Param("institutionCode") String institutionCode, @Param("bankCode") String bankCode);
}
