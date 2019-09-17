package com.payment.task.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Account;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccountMapper extends BaseMapper<Account> {

    /**
     * 查询账户表中结算账户余额减去冻结金额大于0的账户信息
     * @return
     */
    @Select("SELECT id,institution_id as institutionId,institution_name as institutionName,account_code as accountCode,currency,settle_balance as settleBalance,freeze_balance as freezeBalance FROM account WHERE settle_balance-freeze_balance>0 and enabled=1")
    List<Account> getAccounts();
}
