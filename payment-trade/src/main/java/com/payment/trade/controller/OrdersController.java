package com.payment.trade.controller;
import com.payment.common.base.BaseController;
import com.payment.common.dto.*;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.common.vo.DccReportVO;
import com.payment.common.vo.QueryAgencyShareBenefitVO;
import com.payment.common.vo.QueryAgencyTradeVO;
import com.payment.common.vo.ShareBenefitReportVO;
import com.payment.trade.dto.CalcRateDTO;
import com.payment.trade.service.OrdersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * @description: 订单信息接口
 * @author: XuWenQi
 * @create: 2019-03-18 11:50
 **/
@RestController
@Api(description = "订单服务")
@RequestMapping("/orders")
public class OrdersController extends BaseController {

    @Autowired
    private OrdersService ordersService;

    @ApiOperation(value = "换汇金额计算")
    @PostMapping("calcExchangeRate")
    @CrossOrigin
    public BaseResponse calcExchangeRate(@RequestBody @ApiParam @Valid CalcRateDTO calcRateDTO) {
        return ordersService.calcExchangeRate(calcRateDTO);
    }

    @ApiOperation(value = "分页多条件查询订单信息")
    @PostMapping("getByMultipleConditions")
    @CrossOrigin
    public BaseResponse getByMultipleConditions(@RequestBody @ApiParam OrdersDTO ordersDTO) {
        return ResultUtil.success(ordersService.getByMultipleConditions(ordersDTO));
    }

    @ApiOperation(value = "分页多条件查询相关订单全部信息(订单表,退款表，调账表)")
    @PostMapping("getAllOrdersInfo")
    public BaseResponse getAllOrdersInfo(@RequestBody @ApiParam OrdersAllDTO ordersAllDTO) {
        return ResultUtil.success(ordersService.getAllOrdersInfo(ordersAllDTO));
    }

    @ApiOperation(value = "查询交易明细信息")
    @GetMapping("getTradeDetail")
    public BaseResponse getTradeDetail(@RequestParam @ApiParam String id) {
        return ResultUtil.success(ordersService.getTradeDetail(id));
    }

    @ApiOperation(value = "订单信息导出接口", notes = "订单信息导出接口")
    @PostMapping(value = "export")
    public BaseResponse exportInformation(@RequestBody @ApiParam OrdersDTO ordersDTO) {
        return ResultUtil.success(ordersService.exportInformation(ordersDTO));
    }

    @ApiOperation(value = "计算不同支付方式的每日订单金额")
    @PostMapping("calcOrdersAmount")
    public BaseResponse calcOrdersAmount(@RequestBody @ApiParam @Valid CalcOrdersAmountDTO calcOrdersAmountDTO) {
        return ResultUtil.success(ordersService.calcOrdersAmount(calcOrdersAmountDTO));
    }

    @ApiOperation(value = "pos机查询订单打印用")
    @PostMapping("posGetOrders")
    public BaseResponse posGetOrders(@RequestBody @ApiParam PosSearchDTO posSearchDTO) {
        posSearchDTO.setLanguage(this.getLanguage());
        return ResultUtil.success(ordersService.posGetOrders(posSearchDTO));
    }

    @ApiOperation(value = "pos机查询订单详情打印用")
    @PostMapping("posGetOrdersDetail")
    public BaseResponse posGetOrdersDetail(@RequestBody @ApiParam PosSearchDTO posSearchDTO) {
        posSearchDTO.setLanguage(this.getLanguage());
        return ResultUtil.success(ordersService.posGetOrdersDetail(posSearchDTO));
    }

    @ApiOperation(value = "交易信息查询一览导出接口")
    @PostMapping(value = "exportAllOrders")
    public BaseResponse exportAllOrders(@RequestBody @ApiParam OrdersExportAllDTO ordersAllDTO) {
        return ResultUtil.success(ordersService.exportAllOrders(ordersAllDTO));
    }

    @ApiOperation(value = "机构交易一览导出")
    @PostMapping(value = "exportInstitutionOrders")
    public BaseResponse exportInstitutionOrders(@RequestBody @ApiParam OrdersExportAllDTO ordersAllDTO) {
        return ResultUtil.success(ordersService.exportInstitutionOrders(ordersAllDTO));
    }

    @ApiOperation(value = "DCC报表查询")
    @PostMapping("getDccReport")
    public BaseResponse getDccReport(@RequestBody @ApiParam DccReportDTO dccReportDTO) {
        return ResultUtil.success(ordersService.getDccReport(dccReportDTO));
    }

    @ApiOperation(value = "DCC报表导出")
    @PostMapping("exportDccReport")
    public List<DccReportVO> exportDccReport(@RequestBody @ApiParam DccReportExportDTO dccReportExportDTO) {
        return ordersService.exportDccReport(dccReportExportDTO);
    }

    @ApiOperation(value = "代理商交易查询")
    @PostMapping("getAgencyTrade")
    public BaseResponse getAgencyTrade(@RequestBody @ApiParam @Valid QueryAgencyTradeDTO queryAgencyTradeDTO) {
        return ResultUtil.success(ordersService.getAgencyTrade(queryAgencyTradeDTO));
    }

    @ApiOperation(value = "代理商交易导出")
    @PostMapping("exportAgencyTrade")
    public List<QueryAgencyTradeVO> exportAgencyTrade(@RequestBody @ApiParam @Valid ExportAgencyTradeDTO exportAgencyTradeDTO) {
        return ordersService.exportAgencyTrade(exportAgencyTradeDTO);
    }

    @ApiOperation(value = "代理商分润查询")
    @PostMapping("getAgencyShareBenefit")
    public BaseResponse getAgencyShareBenefit(@RequestBody @ApiParam @Valid QueryAgencyShareBenefitDTO queryAgencyShareBenefitDTO) {
        return ResultUtil.success(ordersService.getAgencyShareBenefit(queryAgencyShareBenefitDTO));
    }

    @ApiOperation(value = "代理商分润导出")
    @PostMapping("exportAgencyShareBenefit")
    public List<QueryAgencyShareBenefitVO> exportAgencyShareBenefit(@RequestBody @ApiParam @Valid ExportAgencyShareBenefitDTO exportAgencyShareBenefitDTO) {
        return ordersService.exportAgencyShareBenefit(exportAgencyShareBenefitDTO);
    }

    @ApiOperation(value = "运营后台分润报表查询")
    @PostMapping("getShareBenefitReport")
    public BaseResponse getShareBenefitReport(@RequestBody @ApiParam QueryShareBenefitReportDTO queryShareBenefitReportDTO) {
        return ResultUtil.success(ordersService.getShareBenefitReport(queryShareBenefitReportDTO));
    }

    @ApiOperation(value = "运营后台分润报表导出")
    @PostMapping("exportShareBenefitReport")
    public List<ShareBenefitReportVO> exportShareBenefitReport(@RequestBody @ApiParam ExportShareBenefitReportDTO exportShareBenefitReportDTO) {
        return ordersService.exportShareBenefitReport(exportShareBenefitReportDTO);
    }

}
