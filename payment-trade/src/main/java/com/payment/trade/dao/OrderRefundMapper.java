package com.payment.trade.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.dto.SearchOrderDTO;
import com.payment.common.dto.SearchOrderExportDTO;
import com.payment.common.entity.OrderRefund;
import com.payment.trade.vo.TradeDetailVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRefundMapper extends BaseMapper<OrderRefund> {

    /**
     * @Author YangXu
     * @Date 2019/2/19
     * @Descripate 根据原订单id查询退款总金额
     * @return
     **/
    @Select("select sum(amount) from order_refund where order_id = #{orderId} and refund_status != 3")
    BigDecimal getTotalAmountByOrderId(@Param("orderId") String orderId);

    /**
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 分页查询退款单
     * @return
     **/
    List<OrderRefund>  pageRefundOrder(SearchOrderDTO searchOrderDTO);

    /**
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 导出查询退款接口
     * @return
     *
     * @param searchOrderDTO*/
    List<OrderRefund> exportRefundOrder(SearchOrderExportDTO searchOrderDTO);

    /**
     * 交易明细查询
     *
     * @param id 订单输入实体
     * @return 订单明细输出实体
     */
    TradeDetailVO getTradeDetail(@Param("id") String id, @Param("language") String language);


    /**
     * @Author YangXu
     * @Date 2019/2/28
     * @Descripate 更新退款状态
     * @return
     **/
    @Update("update order_refund set refund_status =#{status},refund_channel_number = #{txnId},update_time= NOW(),remark =#{remark} where id = #{merOrderNo}")
    int updateStatuts(@Param("merOrderNo") String merOrderNo, @Param("status") Byte status, @Param("txnId") String txnId,@Param("remark") String remark);

    /**
     * 更新退款表相关信息
     * @param merOrderNo
     * @param status
     * @param txnId
     * @param remark
     * @param name
     * @return
     */
    @Update("update order_refund set refund_status =#{status},refund_channel_number = #{txnId},update_time= NOW(),remark =#{remark},modifier = #{name} where id = #{merOrderNo}")
    int updateStatutsByName(@Param("merOrderNo") String merOrderNo, @Param("status") Byte status, @Param("txnId") String txnId,@Param("remark") String remark,@Param("name") String name);

    /**
     * @Author YangXu
     * @Date 2019/2/28
     * @Descripate 撤销通道返回失败更新订单备注
     * @return
     **/
    @Update("update order_refund set remark =#{remark},update_time= NOW() where id = #{merOrderNo}")
    int updateRemark(@Param("merOrderNo") String merOrderNo, @Param("remark") String remark);

    /**
     * 更新退款单信息
     * @return
     */
    @Update("update order_refund set refund_status =#{status},refund_channel_number=#{channelNumber},remark =#{remark},update_time=NOW() where id = #{id}")
    int updateRefundOrder(@Param("id") String id,@Param("status") Byte status,@Param("channelNumber") String channelNumber,@Param("remark") String remark);

    /**
     * 撤销时上游通道退款失败更新退款失败的备注
     * @param id
     * @param remark
     * @return
     */
    @Update("update order_refund set remark =#{remark},update_time= NOW() where id = #{id}")
    int updateRefundOrderFail(@Param("id") String id,@Param("remark") String remark);

    /**
     * 根据原订单查询退款单
     * @param orderId 原订单id|
     * @return
     */
    OrderRefund selectByOrderId(String orderId);
}
