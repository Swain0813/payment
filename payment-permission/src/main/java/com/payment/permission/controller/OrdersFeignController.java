package com.payment.permission.controller;

import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.*;
import com.payment.common.entity.Orders;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.common.utils.ArrayUtil;
import com.payment.common.vo.*;
import com.payment.permission.feign.trade.OrdersFeign;
import com.payment.permission.service.OperationLogService;
import com.payment.permission.service.OrdersFeignService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @description:
 * @author: XuWenQi
 * @create: 2019-03-05 15:58
 **/
@RestController
@Api(description = "订单管理接口")
@RequestMapping("/orders")
@Slf4j
public class OrdersFeignController extends BaseController {

    @Autowired
    private OrdersFeign ordersFeign;

    @Autowired
    private OrdersFeignService ordersFeignService;

    @Autowired
    private OperationLogService operationLogService;

    @ApiOperation(value = "分页多条件查询订单信息")
    @PostMapping("getByMultipleConditions")
    public BaseResponse getByMultipleConditions(@RequestBody @ApiParam OrdersDTO ordersDTO) {
        //添加操作日志
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(ordersDTO),
                "查询订单信息"));
        return ordersFeign.getByMultipleConditions(ordersDTO);
    }

    @ApiOperation(value = "查询交易明细信息")
    @GetMapping("getTradeDetail")
    public BaseResponse getTradeDetail(@RequestParam @ApiParam String id, @RequestParam @ApiParam String type) {
        //添加操作日志
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(id),
                "查询交易明细信息"));
        return ordersFeign.getTradeDetail(id, type);
    }

    @ApiOperation(value = "订单信息导出接口", notes = "订单信息导出接口")
    @PostMapping(value = "export")
    public BaseResponse exportOrders(@RequestBody @ApiParam OrdersDTO ordersDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(ordersDTO),
                "订单信息导出"));
        BaseResponse baseResponse = ordersFeign.exportInformation(ordersDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<Orders> orders = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            orders.add(JSON.parseObject(JSON.toJSONString(datum), Orders.class));
        }
        ExcelWriter writer = null;
        try {
            writer = ordersFeignService.getExcelWriter(orders, Orders.class);
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }

    @ApiOperation(value = "计算不同支付方式的每日订单金额")
    @PostMapping("calcOrdersAmount")
    public BaseResponse calcOrdersAmount(@RequestBody @ApiParam @Valid CalcOrdersAmountDTO calcOrdersAmountDTO) {
        //添加操作日志
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(calcOrdersAmountDTO),
                "计算不同支付方式的每日订单金额"));
        return ResultUtil.success(ordersFeign.calcOrdersAmount(calcOrdersAmountDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/8
     * @Descripate pos机查询订单--该接口没用，
     * pos机打印调的是交易模块里的
     **/
    @ApiOperation(value = "pos机查询订单")
    @PostMapping("posGetOrders")
    public BaseResponse posGetOrders(@RequestBody @ApiParam PosSearchDTO posSearchDTO) {
        return ordersFeign.posGetOrders(posSearchDTO);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/8
     * @Descripate pos机查询订单--该接口没用，
     * pos机打印调用的是交易模块的
     **/
    @ApiOperation(value = "pos机查询订单详情")
    @PostMapping("posGetOrdersDetail")
    public BaseResponse posGetOrdersDetail(@RequestBody @ApiParam PosSearchDTO posSearchDTO) {
        return ordersFeign.posGetOrdersDetail(posSearchDTO);
    }

    @ApiOperation(value = "分页多条件查询相关订单全部信息(订单表,退款表，调账表)")
    @PostMapping("getAllOrdersInfo")
    public BaseResponse getAllOrdersInfo(@RequestBody @ApiParam OrdersAllDTO ordersAllDTO) {
        return ordersFeign.getAllOrdersInfo(ordersAllDTO);
    }

    @ApiOperation(value = "交易信息查询一览导出接口", notes = "交易信息查询一览导出接口")
    @PostMapping("exportAllOrders")
    public BaseResponse exportAllOrders(@RequestBody @ApiParam OrdersExportAllDTO ordersAllDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(ordersAllDTO),
                "交易信息查询一览导出接口"));
        BaseResponse baseResponse = ordersFeign.exportAllOrders(ordersAllDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<OrderTradeVO> orderTradeVOS = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            orderTradeVOS.add(JSON.parseObject(JSON.toJSONString(datum), OrderTradeVO.class));
        }
        ExcelWriter writer = null;
        try {
            writer = ordersFeignService.getOrderExcelWriter(orderTradeVOS, OrderTradeVO.class);
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }


    /**
     * 查询机构产品,产品通道信息
     * 给前端模拟界面用的，暂时没用，用的是交易模块里面的
     *
     * @param institutionCode
     * @return
     */
    @ApiOperation(value = "查询机构产品,产品通道信息")
    @GetMapping("getRelevantInfo")
    public BaseResponse get(@RequestParam @ApiParam String institutionCode, HttpServletResponse response) {
        return ordersFeign.getRelevantInfo(institutionCode);
    }

    /**
     * 机构后台交易一览导出接口
     *
     * @param ordersAllDTO
     * @return
     */
    @ApiOperation(value = "机构交易信息查询一览导出接口", notes = "机构交易信息查询一览导出接口")
    @PostMapping("exportInstitutionOrders")
    public BaseResponse exportInstitutionOrders(@RequestBody @ApiParam OrdersExportAllDTO ordersAllDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(ordersAllDTO),
                "机构交易信息查询一览导出接口"));
        BaseResponse baseResponse = ordersFeign.exportAllOrders(ordersAllDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<InstitutionOrderTradeVO> orderTradeVOS = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            orderTradeVOS.add(JSON.parseObject(JSON.toJSONString(datum), InstitutionOrderTradeVO.class));
        }
        ExcelWriter writer = null;
        try {
            if (AsianWalletConstant.EN_US.equals(this.getLanguage())) {//英文的场合
                writer = ordersFeignService.getInstitutionOrderExcelWriter(orderTradeVOS, InstitutionOrderTradeEnVO.class);
            } else {
                writer = ordersFeignService.getInstitutionOrderExcelWriter(orderTradeVOS, InstitutionOrderTradeVO.class);
            }
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }

    @ApiOperation(value = "DCC报表查询")
    @PostMapping("getDccReport")
    public BaseResponse getDccReport(@RequestBody @ApiParam DccReportDTO dccReportDTO) {
        return ordersFeign.getDccReport(dccReportDTO);
    }

    @ApiOperation(value = "DCC报表导出")
    @PostMapping("exportDccReport")
    public BaseResponse exportDccReport(@RequestBody @ApiParam DccReportExportDTO dccReportExportDTO, HttpServletResponse response) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(dccReportExportDTO),
                "DCC报表导出"));
        List<DccReportVO> dccReportList = ordersFeign.exportDccReport(dccReportExportDTO);
        if (dccReportList == null || dccReportList.size() == 0) {
            //数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ExcelWriter writer = null;
        try {
            writer = ordersFeignService.getDccReportExcelWriter(dccReportList, DccReportVO.class);
            writer.flush(response.getOutputStream());
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }

    @ApiOperation(value = "代理商交易查询")
    @PostMapping(value = "/getAgencyTrade")
    public BaseResponse getAgencyTrade(@RequestBody @ApiParam @Valid QueryAgencyTradeDTO queryAgencyTradeDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(queryAgencyTradeDTO),
                "代理商交易查询"));
        return ordersFeign.getAgencyTrade(queryAgencyTradeDTO);
    }

    @ApiOperation(value = "代理商交易导出", notes = "代理商交易导出")
    @PostMapping(value = "/exportAgencyTrade")
    public BaseResponse exportAgencyTrade(@RequestBody @ApiParam @Valid ExportAgencyTradeDTO exportAgencyTradeDTO, HttpServletResponse response) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(exportAgencyTradeDTO),
                "代理商交易导出"));
        List<QueryAgencyTradeVO> queryAgencyTradeVOS = ordersFeign.exportAgencyTrade(exportAgencyTradeDTO);
        if (ArrayUtil.isEmpty(queryAgencyTradeVOS)) {
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ExcelWriter writer = null;
        try {
            if (AsianWalletConstant.EN_US.equals(this.getLanguage())) {
                //英文的场合
                writer = ordersFeignService.exportAgencyTrade(queryAgencyTradeVOS, QueryAgencyTradeEnVO.class);
            } else {
                writer = ordersFeignService.exportAgencyTrade(queryAgencyTradeVOS, QueryAgencyTradeVO.class);
            }
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            log.error("=====================导出异常=====================", e);
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }

    @ApiOperation(value = "代理商分润查询")
    @PostMapping(value = "/getAgencyShareBenefit")
    public BaseResponse getAgencyShareBenefit(@RequestBody @ApiParam @Valid QueryAgencyShareBenefitDTO queryAgencyShareBenefitDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(queryAgencyShareBenefitDTO),
                "代理商分润查询"));
        return ordersFeign.getAgencyShareBenefit(queryAgencyShareBenefitDTO);
    }

    @ApiOperation(value = "代理商分润导出", notes = "代理商分润导出")
    @PostMapping(value = "/exportAgencyShareBenefit")
    public BaseResponse exportAgencyShareBenefit(@RequestBody @ApiParam @Valid ExportAgencyShareBenefitDTO exportAgencyShareBenefitDTO, HttpServletResponse response) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(exportAgencyShareBenefitDTO),
                "代理商分润导出"));
        List<QueryAgencyShareBenefitVO> queryAgencyShareBenefitVOS = ordersFeign.exportAgencyShareBenefit(exportAgencyShareBenefitDTO);
        if (ArrayUtil.isEmpty(queryAgencyShareBenefitVOS)) {
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ExcelWriter writer = null;
        try {
            if (AsianWalletConstant.EN_US.equals(this.getLanguage())) {
                //英文的场合
                writer = ordersFeignService.exportAgencyShareBenefit(queryAgencyShareBenefitVOS, QueryAgencyShareBenefitEnVO.class);
            } else {
                writer = ordersFeignService.exportAgencyShareBenefit(queryAgencyShareBenefitVOS, QueryAgencyShareBenefitVO.class);
            }
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            log.error("=====================导出异常=====================", e);
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }


    @ApiOperation(value = "运营后台分润报表查询")
    @PostMapping(value = "/getShareBenefitReport")
    public BaseResponse getShareBenefitReport(@RequestBody @ApiParam QueryShareBenefitReportDTO queryShareBenefitReportDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(queryShareBenefitReportDTO),
                "运营后台分润报表查询"));
        return ordersFeign.getShareBenefitReport(queryShareBenefitReportDTO);
    }

    @ApiOperation(value = "运营后台分润报表导出", notes = "运营后台分润报表导出")
    @PostMapping(value = "/exportShareBenefitReport")
    public BaseResponse exportShareBenefitReport(@RequestBody @ApiParam ExportShareBenefitReportDTO exportShareBenefitReportDTO, HttpServletResponse response) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(exportShareBenefitReportDTO),
                "运营后台分润报表导出"));
        List<ShareBenefitReportVO> shareBenefitReport = ordersFeign.exportShareBenefitReport(exportShareBenefitReportDTO);
        if (ArrayUtil.isEmpty(shareBenefitReport)) {
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ExcelWriter writer = null;
        try {
            if (AsianWalletConstant.EN_US.equals(this.getLanguage())) {
                //英文的场合
                writer = ordersFeignService.exportShareBenefitReport(shareBenefitReport, ShareBenefitReportEnVO.class);
            } else {
                writer = ordersFeignService.exportShareBenefitReport(shareBenefitReport, ShareBenefitReportVO.class);
            }
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            log.error("=====================导出异常=====================", e);
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }
}
