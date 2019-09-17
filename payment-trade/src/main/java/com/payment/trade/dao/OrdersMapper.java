package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.*;
import com.payment.common.entity.Orders;
import com.payment.common.vo.*;
import com.payment.trade.dto.OnlineqOrderInfoDTO;
import com.payment.trade.dto.PosGetOrdersDTO;
import com.payment.trade.dto.TerminalQueryOrdersDTO;
import com.payment.trade.vo.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 根据商户订单号查询订单信息
     *
     * @param institutionOrderId 商户订单号
     * @return 订单实体
     */
    Orders selectByInstitutionOrderId(@Param("institutionOrderId") String institutionOrderId);

    /**
     * 根据商户订单号与交易状态查询
     *
     * @param institutionOrderId 商户订单号
     * @param tradeStatus        交易状态
     * @return 订单实体
     */
    Orders selectByInstitutionOrderIdAndStatus(@Param("institutionOrderId") String institutionOrderId, @Param("tradeStatus") Byte tradeStatus);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/2/19
     * @Descripate 根据商户编号和商户订单号查询订单信息
     **/
    Orders selectOrderByIdAndCode(@Param("institutionCode") String institutionCode, @Param("institutionOrderId") String institutionOrderId);

    /**
     * 通过id查找订单
     *
     * @param orderId
     * @return
     */
    Orders selectById(@Param("orderId") String orderId);

    /**
     * 多条件查询订单信息
     *
     * @param ordersDTO 订单输入实体
     * @return 订单实体集合
     */
    List<OrdersVO> pageSelectMultipleConditions(OrdersDTO ordersDTO);


    /**
     * 根据订单号修改退款状态
     *
     * @param institutionOrderId
     * @param refundStatus
     * @return
     */
    @Update("update orders set refund_status = #{refundStatus},update_time= NOW() where institution_order_id = #{institutionOrderId} and trade_status = 3")
    int updateOrderRefundStatus(@Param("institutionOrderId") String institutionOrderId, @Param("refundStatus") Byte refundStatus);

    /**
     * 订单信息导出
     *
     * @param ordersDTO 订单输入实体
     * @return 订单输出实体集合
     */
    List<Orders> selectExport(OrdersDTO ordersDTO);

    /**
     * 交易明细查询
     *
     * @param id 订单输入实体
     * @return 订单明细输出实体
     */
    TradeDetailVO getTradeDetail(@Param("id") String id, @Param("language") String language);


    /**
     * 计算订单金额
     *
     * @param calcOrdersAmountDTO 计算订单金额输入实体
     * @return 订单金额输出实体集合
     */
    List<OrdersAmountVO> calcOrdersAmount(CalcOrdersAmountDTO calcOrdersAmountDTO);

    /**
     * 根据AD3的查询订单信息更新亚洲钱包的订单信息状态
     *
     * @return
     */
    @Update("update orders set trade_status =#{status},channel_number=#{channelNumber},channel_callback_time=#{channelCallbackTime},update_time=NOW() where id = #{id} and trade_status=2")
    int updateOrderByAd3Query(@Param("id") String id, @Param("status") Byte status, @Param("channelNumber") String channelNumber, @Param("channelCallbackTime") Date channelCallbackTime);

    /**
     * 更新撤销中订单通道返回失败的备注
     * 更新撤销状态是撤销中的订单信息
     *
     * @param id
     * @param remark
     * @return
     */
    @Update("update orders set remark =#{remark},update_time= NOW() where id = #{id} and cancel_status=1")
    int updateCancelOrderRemark(@Param("id") String id, @Param("remark") String remark);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/8
     * @Descripate pos机查询订单
     **/
    List<PosSearchVO> posGetOrders(PosSearchDTO posSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/8
     * @Descripate pos机查询订单详情
     **/
    List<PosSearchVO> posGetOrdersDetail(PosSearchDTO posSearchDTO);


    /**
     * pos机查询订单信息
     *
     * @param posGetOrdersDTO 订单输入实体
     * @return 订单输出实体集合
     */
    List<PosOrdersVO> pageGetOrdersInfo(PosGetOrdersDTO posGetOrdersDTO);

    /**
     * 线下查询订单信息
     *
     * @param posGetOrdersDTO 订单输入实体
     * @return 订单输出实体集合
     */
    List<OfflineOrdersVO> pageOfflineOrdersInfo(PosGetOrdersDTO posGetOrdersDTO);


    /**
     * 根据商户订单号更新订单信息表中的撤销状态以及更新人
     *
     * @param institutionOrderId
     * @param deviceOperator
     * @param cancelStatus
     */
    @Update("update orders set cancel_status=#{cancelStatus},modifier=#{deviceOperator},update_time=NOW() where institution_order_id = #{institutionOrderId} and trade_status in (2,3)")
    int updateOrderCancelStatus(@Param("institutionOrderId") String institutionOrderId, @Param("deviceOperator") String deviceOperator, @Param("cancelStatus") Byte cancelStatus);


    /**
     * 分页多条件查询相关订单全部信息(订单表,退款表，调账表)
     *
     * @param ordersAllDTO
     * @return
     */
    List<OrderTradeVO> pageGetAllOrdersInfo(OrdersAllDTO ordersAllDTO);

    /**
     * 导出查询相关订单全部信息
     *
     * @param ordersAllDTO
     * @return
     */
    List<OrderTradeVO> exportAllOrdersInfo(OrdersExportAllDTO ordersAllDTO);

    /**
     * 依据时间降序去查找最近一条的订单信息
     *
     * @param institutionOrderId
     * @return
     */
    Orders selectOrderByInstitutionOrderId(@Param("institutionOrderId") String institutionOrderId);

    /**
     * 计算机构不同产品品的每日成功订单的总订单金额,与产品总金额
     *
     * @param calcOrdersAmountDTO
     * @return
     */
    List<CalcInsOrdersAmountVO> calcInsOrdersDailyAmount(CalcOrdersAmountDTO calcOrdersAmountDTO);

    /**
     * 查询不同支付方式的总金额
     *
     * @param calcOrdersAmountDTO
     * @return
     */
    List<CalcTotalAmountVO> calcInsOrdersTotalAmount(CalcOrdersAmountDTO calcOrdersAmountDTO);

    /**
     * 查询不同币种的总金额
     *
     * @param calcOrdersAmountDTO
     * @return
     */
    List<CalcCurrencyAmountVO> selectCurrencyTotalAmount(CalcOrdersAmountDTO calcOrdersAmountDTO);

    /**
     * 查询线上订单
     *
     * @param onlineqOrderInfoDTO
     * @return
     */
    List<OnlineOrdersInfoVO> pageOnlineOrderInfo(OnlineqOrderInfoDTO onlineqOrderInfoDTO);

    /**
     * 【内部接口-POS机】查询订单详情
     *
     * @param terminalQueryDTO
     * @return
     */
    PosOrdersVO terminalQueryOrderDetail(TerminalQueryOrdersDTO terminalQueryDTO);

    /**
     * 机构交易一览导出
     *
     * @param ordersAllDTO
     * @return
     */
    List<InstitutionOrderTradeVO> exportInstitutionOrders(OrdersExportAllDTO ordersAllDTO);

    /**
     * pos机分页查询订单列表
     *
     * @param posGetOrdersDTO
     * @return
     */
    List<PosOrdersVO> pagePosGetOrdersInfo(PosGetOrdersDTO posGetOrdersDTO);

    /**
     * DCC报表查询
     *
     * @param dccReportDTO dcc报表查询实体
     * @return DccReportVO
     */
    List<DccReportVO> pageDccReport(DccReportDTO dccReportDTO);

    /**
     * DCC报表查询
     *
     * @param dccReportExportDTO dcc报表查询实体
     * @return DccReportVO
     */
    List<DccReportVO> exportDccReport(DccReportExportDTO dccReportExportDTO);

    /**
     * 更新订单 回调失败原因
     *
     * @param referenceNo
     * @param msg
     */
    void updateOrderRemark(@Param("referenceNo") String referenceNo, @Param("msg") String msg);

    /**
     * 代理商交易查询
     *
     * @param queryAgencyTradeDTO 代理商交易查询DTO
     * @return
     */
    List<QueryAgencyTradeVO> pageAgencyTrade(QueryAgencyTradeDTO queryAgencyTradeDTO);

    /**
     * 代理商交易导出
     *
     * @param exportAgencyTradeDTO 代理商交易查询DTO
     * @return
     */
    List<QueryAgencyTradeVO> exportAgencyTrade(ExportAgencyTradeDTO exportAgencyTradeDTO);
}
