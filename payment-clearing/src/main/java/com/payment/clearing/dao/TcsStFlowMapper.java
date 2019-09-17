package com.payment.clearing.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.TcsStFlow;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface TcsStFlowMapper extends BaseMapper<TcsStFlow> {


    /**
     * @Author YangXu
     * @Date 2019/7/29
     * @Descripate 查询所有date时间之前的未结算订单
     * @return
     **/
    List<TcsStFlow> selectList( TcsStFlow tcsStFlow );
}
