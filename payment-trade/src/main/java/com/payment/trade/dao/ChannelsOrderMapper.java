package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.ChannelsOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelsOrderMapper extends BaseMapper<ChannelsOrder> {

    /**
     * 根据id和交易状态更新上报通道订单记录中的通道流水号
     * @param orderId
     * @param channelNumber
     * @param status
     * @return
     */
    @Update("update channels_order set trade_status = #{status},channel_number = #{channelNumber},update_time= NOW() where id = #{orderId} and trade_status = 1")
    int updateStatusById(@Param("orderId") String orderId, @Param("channelNumber") String channelNumber, @Param("status") String status);

    /**
     * 根据id和交易状态更新上报通道订单记录中的备注
     * @param remark
     * @param status
     * @return
     */
    @Update("update channels_order set remark = #{remark} update_time= NOW() where id = #{orderId} and trade_status = 1")
    int updateRemarkById(@Param("remark") String remark, @Param("status") String status);

    /**
     * 根据备注1查询订单
     *
     * @param remark1 备注1
     * @return 通道订单
     */
    ChannelsOrder selectByRemarks(@Param("remark1") String remark1, @Param("remark2") String remark2, @Param("remark3") String remark3);

}
