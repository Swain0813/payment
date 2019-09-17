package com.payment.trade.service;


import com.payment.common.dto.*;
import com.payment.common.entity.Orders;
import com.payment.common.response.BaseResponse;
import com.payment.common.vo.*;
import com.payment.trade.dto.CalcRateDTO;
import com.payment.trade.vo.OrdersVO;
import com.payment.trade.vo.StatisticsVO;
import com.payment.trade.vo.TradeDetailVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @Author XuWenQi
 * @Date 2019/2/18 15:14
 * @Descripate 订单业务接口
 */
public interface OrdersService {

    /**
     * 多条件查询订单信息
     *
     * @param ordersDTO 订单输入实体
     * @return 订单输出实体集合
     */
    PageInfo<OrdersVO> getByMultipleConditions(OrdersDTO ordersDTO);

    /**
     * 订单导出
     *
     * @param ordersDTO 订单输入实体
     * @return 订单输出实体集合
     */
    List<Orders> exportInformation(OrdersDTO ordersDTO);

    /**
     * 交易明细查询
     *
     * @param id 订单id
     * @return 订单输出实体集合
     */
    TradeDetailVO getTradeDetail(String id);

    /**
     * 换汇金额计算
     *
     * @param calcRateDTO 订单输入实体
     * @return 换汇计算输出实体
     */
    BaseResponse calcExchangeRate(CalcRateDTO calcRateDTO);


    /**
     * 计算机构不同产品品的每日成功订单的总订单金额,与产品总金额
     *
     * @param calcOrdersAmountDTO 订单输入实体
     * @return
     */
    StatisticsVO calcOrdersAmount(CalcOrdersAmountDTO calcOrdersAmountDTO);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/8
     * @Descripate pos机查询订单打印用
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
     * 分页查询订单表信息，退款表信息以及调账表信息
     *
     * @param ordersAllDTO
     * @return
     */
    PageInfo<OrderTradeVO> getAllOrdersInfo(OrdersAllDTO ordersAllDTO);


    /**
     * 交易一览导出相关订单信息
     *
     * @param ordersAllDTO 订单输入实体
     * @return 订单输出实体集合
     */
    List<OrderTradeVO> exportAllOrders(OrdersExportAllDTO ordersAllDTO);

    /**
     * 机构交易一览导出
     *
     * @param ordersAllDTO
     * @return
     */
    List<InstitutionOrderTradeVO> exportInstitutionOrders(OrdersExportAllDTO ordersAllDTO);


    /**
     * DCC报表查询
     *
     * @param dccReportDTO dcc报表查询实体
     * @return DccReportVO
     */
    PageInfo<DccReportVO> getDccReport(DccReportDTO dccReportDTO);

    /**
     * DCC报表导出
     *
     * @param dccReportExportDTO dcc报表导出实体
     * @return DccReportVO
     */
    List<DccReportVO> exportDccReport(DccReportExportDTO dccReportExportDTO);

    /**
     * 代理商交易查询
     *
     * @param queryAgencyTradeDTO 代理商交易查询DTO
     * @return QueryAgencyTradeVO
     */
    PageInfo<QueryAgencyTradeVO> getAgencyTrade(QueryAgencyTradeDTO queryAgencyTradeDTO);

    /**
     * 代理商交易导出
     *
     * @param exportAgencyTradeDTO 代理商交易查询DTO
     * @return QueryAgencyTradeVO
     */
    List<QueryAgencyTradeVO> exportAgencyTrade(ExportAgencyTradeDTO exportAgencyTradeDTO);

    /**
     * 代理商分润查询
     *
     * @param queryAgencyShareBenefitDTO queryAgencyShareBenefitDTO
     * @return QueryAgencyShareBenefitVO
     */
    PageInfo<QueryAgencyShareBenefitVO> getAgencyShareBenefit(QueryAgencyShareBenefitDTO queryAgencyShareBenefitDTO);

    /**
     * 代理商分润导出
     *
     * @param exportAgencyShareBenefitDTO exportAgencyShareBenefitDTO
     * @return QueryAgencyShareBenefitVO
     */
    List<QueryAgencyShareBenefitVO> exportAgencyShareBenefit(ExportAgencyShareBenefitDTO exportAgencyShareBenefitDTO);

    /**
     * 运营后台分润报表查询
     *
     * @param queryShareBenefitReportDTO queryShareBenefitReportDTO
     * @return QueryAgencyShareBenefitVO
     */
    PageInfo<ShareBenefitReportVO> getShareBenefitReport(QueryShareBenefitReportDTO queryShareBenefitReportDTO);

    /**
     * 运营后台分润报表导出
     *
     * @param exportShareBenefitReportDTO exportShareBenefitReportDTO
     * @return QueryAgencyShareBenefitVO
     */
    List<ShareBenefitReportVO> exportShareBenefitReport(ExportShareBenefitReportDTO exportShareBenefitReportDTO);
}
