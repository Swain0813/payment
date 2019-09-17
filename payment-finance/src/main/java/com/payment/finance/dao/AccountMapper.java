package com.payment.finance.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Account;
import com.payment.finance.entity.WithdrawalVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountMapper extends BaseMapper<Account> {

    /**
     * 获取账户信息
     *
     * @param institutionId
     * @param currency
     * @return
     */
    @Select("select id,account_code as accountCode,institution_id as institutionId,settle_balance as settleBalance,freeze_balance as freezeBalance,enabled as enabled from account where institution_id = #{institutionId} and currency = #{currency}")
    Account getAccount(@Param("institutionId") String institutionId, @Param("currency") String currency);

    /**
     * 查询账户表中结算账户余额减去冻结金额大于最小起结金额的账户信息
     *
     * @return
     * @param institutionCode
     * @param currency
     */
    WithdrawalVO getAccountByWithdrawal(@Param("institutionCode") String institutionCode, @Param("currency") String currency);
}
