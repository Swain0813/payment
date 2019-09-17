package com.payment.clearing.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.TcsCtFlow;
import org.springframework.stereotype.Repository;

@Repository
public interface TcsCtFlowMapper extends BaseMapper<TcsCtFlow> {


    /**
     * 结算处理前查询当前待结算数据还有多少可以处理的
     * 清算资金，主要用在订单有RV的情况下清算资金检查
     * @return
     */
    Double getCLLeftMoney(TcsCtFlow ctflow);
}
