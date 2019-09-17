package com.payment.permission.feign.trade;

import com.payment.common.dto.*;
import com.payment.common.response.BaseResponse;
import com.payment.common.vo.DccReportVO;
import com.payment.common.vo.QueryAgencyShareBenefitVO;
import com.payment.common.vo.QueryAgencyTradeVO;
import com.payment.common.vo.ShareBenefitReportVO;
import com.payment.permission.feign.trade.impl.OrdersFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@FeignClient(value = "payment-trade", fallback = OrdersFeignImpl.class)
public interface OrdersFeign {

    /**
     * 导出Excel
     *
     * @param ordersDTO 订单输入实体
     * @return
     */
    @PostMapping(value = "/orders/export")
    BaseResponse exportInformation(@RequestBody @ApiParam OrdersDTO ordersDTO);

    /**
     * 分页多条件查询订单信息
     *
     * @param ordersDTO 订单输入实体
     * @return
     */
    @PostMapping("/orders/getByMultipleConditions")
    BaseResponse getByMultipleConditions(@RequestBody @ApiParam OrdersDTO ordersDTO);


    /**
     * 根据id查询订单明细信息
     *
     * @param id 订单id
     * @return
     */
    @GetMapping("/orders/getTradeDetail")
    BaseResponse getTradeDetail(@RequestParam("id") @ApiParam String id, @RequestParam("type") @ApiParam String type);


    /**
     * 计算不同支付方式的每日订单金额
     *
     * @param calcOrdersAmountDTO 订单输入实体
     * @return
     */
    @PostMapping("/orders/calcOrdersAmount")
    BaseResponse calcOrdersAmount(@RequestBody @ApiParam @Valid CalcOrdersAmountDTO calcOrdersAmountDTO);

    @PostMapping("/orders/posGetOrders")
    BaseResponse posGetOrders(@RequestBody @ApiParam PosSearchDTO posSearchDTO);

    @PostMapping("/orders/posGetOrdersDetail")
    BaseResponse posGetOrdersDetail(@RequestBody @ApiParam PosSearchDTO posSearchDTO);

    /**
     * 分页多条件查询相关订单全部信息(订单表,退款表，调账表)
     *
     * @param ordersAllDTO
     * @return
     */
    @PostMapping("/orders/getAllOrdersInfo")
    BaseResponse getAllOrdersInfo(@RequestBody @ApiParam OrdersAllDTO ordersAllDTO);

    /**
     * 交易信息查询一览导出接口
     *
     * @param ordersAllDTO
     * @return
     */
    @PostMapping("/orders/exportAllOrders")
    BaseResponse exportAllOrders(@RequestBody @ApiParam OrdersExportAllDTO ordersAllDTO);

    @GetMapping("/trade/getRelevantInfo")
    BaseResponse getRelevantInfo(@RequestParam("institutionCode") @ApiParam String institutionCode);

    /**
     * 机构交易一览导出
     *
     * @param ordersAllDTO
     * @return
     */
    @PostMapping("/orders/exportInstitutionOrders")
    BaseResponse exportInstitutionOrders(@RequestBody @ApiParam OrdersExportAllDTO ordersAllDTO);

    /**
     * DCC报表查询
     *
     * @param dccReportDTO dcc报表查询实体
     * @return DccReportVO
     */
    @PostMapping("/orders/getDccReport")
    BaseResponse getDccReport(DccReportDTO dccReportDTO);

    /**
     * DCC报表导出
     *
     * @param dccReportExportDTO dcc报表查询实体
     * @return DccReportVO
     */
    @PostMapping("/orders/exportDccReport")
    List<DccReportVO> exportDccReport(DccReportExportDTO dccReportExportDTO);

    /**
     * 代理商交易查询
     *
     * @param queryAgencyTradeDTO queryAgencyTradeDTO
     * @return BaseResponse
     */
    @PostMapping("/orders/getAgencyTrade")
    BaseResponse getAgencyTrade(QueryAgencyTradeDTO queryAgencyTradeDTO);

    /**
     * 代理商分润导出
     *
     * @param exportAgencyTradeDTO exportAgencyTradeDTO
     * @return QueryAgencyTradeVO
     */
    @PostMapping("/orders/exportAgencyTrade")
    List<QueryAgencyTradeVO> exportAgencyTrade(ExportAgencyTradeDTO exportAgencyTradeDTO);

    /**
     * 代理商分润查询
     *
     * @param queryAgencyShareBenefitDTO queryAgencyShareBenefitDTO
     * @return BaseResponse
     */
    @PostMapping("/orders/getAgencyShareBenefit")
    BaseResponse getAgencyShareBenefit(QueryAgencyShareBenefitDTO queryAgencyShareBenefitDTO);

    /**
     * 代理商交易导出
     *
     * @param exportAgencyShareBenefitDTO exportAgencyShareBenefitDTO
     * @return QueryAgencyTradeVO
     */
    @PostMapping("/orders/exportAgencyShareBenefit")
    List<QueryAgencyShareBenefitVO> exportAgencyShareBenefit(ExportAgencyShareBenefitDTO exportAgencyShareBenefitDTO);


    /**
     * 运营后台分润报表查询
     *
     * @param queryShareBenefitReportDTO queryShareBenefitReportDTO
     * @return QueryAgencyTradeVO
     */
    @PostMapping("/orders/getShareBenefitReport")
    BaseResponse getShareBenefitReport(QueryShareBenefitReportDTO queryShareBenefitReportDTO);

    /**
     * 运营后台分润报表导出
     *
     * @param exportShareBenefitReportDTO exportShareBenefitReportDTO
     * @return QueryAgencyTradeVO
     */
    @PostMapping("/orders/exportShareBenefitReport")
    List<ShareBenefitReportVO> exportShareBenefitReport(ExportShareBenefitReportDTO exportShareBenefitReportDTO);
}
