package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.TcsStFlow;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;


@Repository
public interface TcsStFlowMapper extends BaseMapper<TcsStFlow> {


    /**
     * @Author YangXu
     * @Date 2019/3/13
     * @Descripate 根据系统订单号查询结算状态: 1-未结算，2-已结算
     * @return
     **/
    @Select("select STstate from tcs_stflow where sysorderid = #{sysorderid} and refcnceFlow = #{sysorderid} and tradetype = 'ST'")
    Integer getSTstatus(String sysorderid);

}
