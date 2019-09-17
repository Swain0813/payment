package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.TcsCtFlow;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface TcsCtFlowMapper extends BaseMapper<TcsCtFlow> {


    /**
     * @Author YangXu
     * @Date 2019/3/13
     * @Descripate 根据系统订单号查询清算状态:1-待清算，2-已清算
     * @return
     **/
    @Select("select CTstate from tcs_ctflow where sysorderid = #{sysorderid} and refcnceFlow = #{sysorderid}  and tradetype = 'NT'")
    Integer getCTstatus(String sysorderid);

}
