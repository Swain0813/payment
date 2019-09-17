package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.SettleControl;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SettleControlMapper extends BaseMapper<SettleControl> {
    /**
     * 根据账户ID去查找对应的控制参数
     *
     * @param accountId
     * @return
     */
    SettleControl selectByAccountId(@Param("accountId") String accountId);
}
