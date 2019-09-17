package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Bank;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BankMapper extends BaseMapper<Bank> {

    /**
     * 依据id查找银行
     *
     * @param bankId
     * @return
     */
    Bank selectById(@Param("bankId") String bankId);

    /**
     * 依据issuerId 查找银行名
     *
     * @param issuerId
     * @return
     */
    String selectByIssuerId(@Param("issuerId") String issuerId);

}
