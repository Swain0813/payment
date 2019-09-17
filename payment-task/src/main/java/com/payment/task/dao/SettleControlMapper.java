package com.payment.task.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.SettleControl;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface SettleControlMapper extends BaseMapper<SettleControl> {

    /**
     * 根据accountId查询账户关联表信息
     *
     * @param accountId
     * @return
     */
    @Select("SELECT min_settle_amount as  minSettleAmount FROM settle_control WHERE account_id = #{accountId} AND settle_switch=1 AND enabled=1")
    SettleControl selectByAccountId(@Param("accountId") String accountId);
}
