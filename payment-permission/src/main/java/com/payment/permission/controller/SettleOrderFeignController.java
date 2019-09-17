package com.payment.permission.controller;

import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.ReviewSettleDTO;
import com.payment.common.dto.SettleOrderDTO;
import com.payment.common.dto.SettleOrderExportDTO;
import com.payment.common.dto.WithdrawalDTO;
import com.payment.common.entity.SettleOrder;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.permission.dto.SettleOrderExport;
import com.payment.permission.dto.SettleOrderInsEnExport;
import com.payment.permission.dto.SettleOrderInsExport;
import com.payment.permission.feign.finance.SettleOrderFeign;
import com.payment.permission.service.OperationLogService;
import com.payment.permission.service.SettleOrderFeignService;
import com.payment.permission.service.SysUserVoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * 机构结算交易相关模块
 */
@RestController
@Api(description = "机构结算交易接口")
@RequestMapping("/settleorder")
@Slf4j
public class SettleOrderFeignController extends BaseController {

    @Autowired
    private SettleOrderFeign settleOrderFeign;

    @Autowired
    private SettleOrderFeignService settleOrderFeignService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private SysUserVoService sysUserVoService;

    @ApiOperation(value = "机构结算交易分页查询一览")
    @PostMapping("/pageSettleOrder")
    public BaseResponse pageSettleOrder(@RequestBody @ApiParam SettleOrderDTO settleOrderDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(settleOrderDTO),
                "机构结算交易分页查询一览"));
        return settleOrderFeign.pageSettleOrder(settleOrderDTO);
    }

    @ApiOperation(value = "机构结算交易分页查询详情")
    @PostMapping("/pageSettleOrderDetail")
    public BaseResponse pageSettleOrderDetail(@RequestBody @ApiParam SettleOrderDTO settleOrderDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(settleOrderDTO),
                "机构结算交易分页查询详情"));
        return settleOrderFeign.pageSettleOrderDetail(settleOrderDTO);
    }

    /**
     * 后台机构结算审核导出
     * @param settleOrderDTO
     * @return
     */
    @ApiOperation(value = "机构结算审核导出")
    @PostMapping("/exportSettleOrder")
    public BaseResponse exportSettleOrder(@RequestBody @ApiParam SettleOrderDTO settleOrderDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(settleOrderDTO),
                "机构结算审核导出"));
        BaseResponse baseResponse = settleOrderFeign.exportSettleOrder(settleOrderDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<SettleOrder> settleOrders = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            settleOrders.add(JSON.parseObject(JSON.toJSONString(datum), SettleOrder.class));
        }
        ExcelWriter writer = null;
        try {
            writer = settleOrderFeignService.getExcelWriter(settleOrders, SettleOrderExport.class);
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            log.info("-----------------文件导出异常-----------------exception:{}", JSON.toJSONString(e));
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return ResultUtil.success();
    }

    @ApiOperation(value = "机构结算审核")
    @PostMapping("/reviewSettlement")
    public BaseResponse reviewSettlement(@RequestBody @ApiParam ReviewSettleDTO reviewSettleDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(reviewSettleDTO),
                "机构结算审核"));
        //校验交易密码
        if (!sysUserVoService.checkPassword(sysUserVoService.decryptPassword(reviewSettleDTO.getTradePwd()), this.getSysUserVO().getTradePassword())) {
            throw new BusinessException(EResultEnum.TRADE_PASSWORD_ERROR.getCode());
        }
        return settleOrderFeign.reviewSettlement(reviewSettleDTO);
    }


    @ApiOperation(value = "机构结算查询一览详情")
    @PostMapping("/pageSettleOrderQuery")
    public BaseResponse pageSettleOrderQuery(@RequestBody @ApiParam SettleOrderExportDTO settleOrderDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(settleOrderDTO),
                "机构结算查询一览详情"));
        return settleOrderFeign.pageSettleOrderQuery(settleOrderDTO);
    }

    @ApiOperation(value = "手动跑批")
    @PostMapping("/withdrawal")
    public BaseResponse withdrawal(@RequestBody @ApiParam WithdrawalDTO withdrawalDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(withdrawalDTO),
                "手动跑批"));
        //校验交易密码
        if (!sysUserVoService.checkPassword(withdrawalDTO.getTradePwd(), this.getSysUserVO().getTradePassword())) {
            throw new BusinessException(EResultEnum.TRADE_PASSWORD_ERROR.getCode());
        }
        return settleOrderFeign.withdrawal(withdrawalDTO);
    }

    /**
     * 机构后台系统提款查询一览导出功能
     *
     * @param settleOrderDTO
     * @return
     */
    @ApiOperation(value = "机构系统机构结算审核导出")
    @PostMapping("/exportInsSettleOrder")
    public BaseResponse exportInsSettleOrder(@RequestBody @ApiParam SettleOrderDTO settleOrderDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(settleOrderDTO),
                "机构系统机构结算审核导出"));
        BaseResponse baseResponse = settleOrderFeign.exportSettleOrder(settleOrderDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<SettleOrder> settleOrders = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            settleOrders.add(JSON.parseObject(JSON.toJSONString(datum), SettleOrder.class));
        }
        ExcelWriter writer = null;
        try {
            if (AsianWalletConstant.EN_US.equals(this.getLanguage())) {//英文版的场合
                writer = settleOrderFeignService.getInsExcelWriter(settleOrders, SettleOrderInsEnExport.class);
            } else {
                writer = settleOrderFeignService.getInsExcelWriter(settleOrders, SettleOrderInsExport.class);
            }
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            log.info("-----------------文件导出异常-----------------exception:{}", JSON.toJSONString(e));
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return ResultUtil.success();
    }
}
