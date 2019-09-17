package com.payment.finance.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.TcsStFlow;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface TcsStFlowMapper extends BaseMapper<TcsStFlow> {


    /**
     * @Author YangXu
     * @Date 2019/4/16
     * @Descripate 查询当前时间所有结算订单
     * @return
     **/
    List<TcsStFlow> selectTcsStFlow(@Param("time") Date time);
}
