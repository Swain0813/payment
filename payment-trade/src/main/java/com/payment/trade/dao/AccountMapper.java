package com.payment.trade.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Account;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;

@Repository
public interface AccountMapper extends BaseMapper<Account> {

    /**
     * 根据机构code和币种查询账户的结算金额
     * @param institutionCode
     * @param currency
     * @return
     */
    @Select("select settle_balance from account  where institution_id = #{institutionCode} and currency = #{currency} and enabled=true")
    BigDecimal getBalance(@Param("institutionCode") String institutionCode, @Param("currency") String currency);

    /**
     * 根据机构code和币种查询账户的结算金额和冻结金额
     * @param institutionCode
     * @param currency
     * @return
     */
    @Select("select id,settle_balance as settleBalance,freeze_balance as freezeBalance from account  where institution_id = #{institutionCode} and currency = #{currency} and enabled=true")
    Account getAccount(@Param("institutionCode") String institutionCode, @Param("currency") String currency);
}
