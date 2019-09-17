package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.BankIssuerid;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankIssueridMapper extends BaseMapper<BankIssuerid> {


    List<BankIssuerid> selectByIssuerId(@Param("issuerId") String issuerId);

    /**
     * 通过通道币种,银行名称,通道Code,查找映射表
     *
     * @param Currency
     * @param bankName
     * @param channelCode
     * @return
     */
    BankIssuerid selectBankAndIssuerId(@Param("bankCurrency") String Currency, @Param("bankName") String bankName, @Param("channelCode") String channelCode);

    /**
     * 通过通道Code 与 通道币种来查询映射表
     *
     * @param channelCode
     * @param currency
     * @return
     */
    BankIssuerid selectByChannelCodeAndCurrency(@Param("channelCode") String channelCode, @Param("currency") String currency);

    /**
     * 通过通道CODE查询映射表
     *
     * @param channelCode
     * @return
     */
    List<BankIssuerid> selectByChannelCode(@Param("channelCode") String channelCode);
}
