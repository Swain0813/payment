package com.payment.permission.controller;

import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.PaymentModeDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.permission.feign.institution.PaymentModeFeign;
import com.payment.permission.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shenxinran
 * @Date: 2019/3/3 18:40
 * @Description: 支付方式管理
 */
@RestController
@Api(description = "支付方式管理接口")
@RequestMapping("/paymentmode")
public class PaymentModeFeignController extends BaseController {

    @Autowired
    private PaymentModeFeign paymentModeFeign;

    /**
     * 操作日志模块
     */
    @Autowired
    private OperationLogService operationLogService;

    @ApiOperation(value = "添加支付方式")
    @PostMapping("addPayinfo")
    public BaseResponse addPayinfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(paymentModeDTO),
                "添加支付方式"));
        return paymentModeFeign.addPayinfo(paymentModeDTO);
    }

    @ApiOperation(value = "添加不同语言的支付方式")
    @PostMapping("addOtherLanguage")
    public BaseResponse addOtherLanguage(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(paymentModeDTO),
                "添加不同语言的支付方式"));
        return ResultUtil.success(paymentModeFeign.addOtherLanguage(paymentModeDTO));
    }

    @ApiOperation(value = "查询支付方式")
    @PostMapping("pagePayInfo")
    public BaseResponse pagePayInfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(paymentModeDTO),
                "查询支付方式"));
        return paymentModeFeign.pagePayInfo(paymentModeDTO);
    }

    @ApiOperation(value = "查询所有支付方式")
    @PostMapping("getPayInfo")
    public BaseResponse getPayInfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(paymentModeDTO),
                "查询所有支付方式"));
        return paymentModeFeign.getPayInfo(paymentModeDTO);
    }

    @ApiOperation(value = "更新支付方式")
    @PostMapping("updatePayInfo")
    public BaseResponse updatePayInfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(paymentModeDTO),
                "更新支付方式"));
        return paymentModeFeign.updatePayInfo(paymentModeDTO);
    }

    @ApiOperation(value = "启用禁用支付方式")
    @PostMapping("banPayInfo")
    public BaseResponse banPayInfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(paymentModeDTO),
                "启用禁用支付方式"));
        return paymentModeFeign.banPayInfo(paymentModeDTO);
    }
}
