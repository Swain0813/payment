package com.payment.finance.dao;


import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Orders;
import com.payment.finance.vo.CheckAccountVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface OrdersMapper extends BaseMapper<Orders> {


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/29
     * @Descripate 查询对应渠道响应时间的订单
     **/
    List<Orders> getYesterDayDate(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("list") List<String> list);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/9
     * @Descripate 补单操作
     **/
    @Update("update orders set trade_status =#{status},remark1=#{remark},update_time=NOW() where id = #{id}")
    int supplementStatus(@Param("id") String id, @Param("status") Byte status, @Param("remark") String remark);

    /**
     * 根据日期与语言查询订单信息
     *
     * @param yesterday 昨日日期
     * @return
     */
    List<Orders> selectByDate(String yesterday);


    /**
     * 根据日期计算不同币种的成功订单与退款订单总金额与总笔数
     *
     * @param yesterday 昨日日期
     * @return
     */
    List<CheckAccountVO> tradeAccountCheck(@Param("yesterday") String yesterday);

}
