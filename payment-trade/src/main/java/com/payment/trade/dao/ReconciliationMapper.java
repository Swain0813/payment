package com.payment.trade.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Reconciliation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface ReconciliationMapper  extends BaseMapper<Reconciliation> {

    /**
     * @Author YangXu
     * @Date 2019/3/26
     * @Descripate 根据id修改调账单状态
     * @return
     **/
    @Update("update reconciliation set status = #{status} where id = #{id}")
    int updateStatusById(@Param("id") String id ,@Param("status") int status);
}
