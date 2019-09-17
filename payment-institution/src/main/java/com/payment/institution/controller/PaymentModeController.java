package com.payment.institution.controller;

import com.payment.common.base.BaseController;
import com.payment.common.dto.PaymentModeDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.institution.service.PaymentModeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shenxinran
 * @Date: 2019/3/3 13:43
 * @Description: 支付方式管理Controller
 */
@RestController
@Api(description = "支付方式管理接口")
@RequestMapping("/paymentmode")
public class PaymentModeController extends BaseController {

    @Autowired
    private PaymentModeService paymentModeService;

    @ApiOperation(value = "添加支付方式")
    @PostMapping("addPayinfo")
    public BaseResponse addPayinfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO) {
            paymentModeDTO.setCreator(this.getSysUserVO().getUsername());
        return ResultUtil.success(paymentModeService.addPayinfo(paymentModeDTO));
    }

    @ApiOperation(value = "添加不同语言的支付方式")
    @PostMapping("addOtherLanguage")
    public BaseResponse addOtherLanguage(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO) {
        paymentModeDTO.setCreator(this.getSysUserVO().getUsername());
        return ResultUtil.success(paymentModeService.addOtherLanguage(paymentModeDTO));
    }

    @ApiOperation(value = "查询支付方式")
    @PostMapping("pagePayInfo")
    public BaseResponse pagePayInfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO) {
        return ResultUtil.success(paymentModeService.pagePayInfo(paymentModeDTO));
    }

    @ApiOperation(value = "查询所有支付方式")
    @PostMapping("getPayInfo")
    public BaseResponse getPayInfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO) {
        if (StringUtils.isBlank(paymentModeDTO.getLanguage())) {
            paymentModeDTO.setLanguage(this.getLanguage());
        }
        return ResultUtil.success(paymentModeService.getPayInfo(paymentModeDTO));
    }

    @ApiOperation(value = "更新支付方式")
    @PostMapping("updatePayInfo")
    public BaseResponse updatePayInfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO) {
        paymentModeDTO.setModifier(this.getSysUserVO().getUsername());
        return ResultUtil.success(paymentModeService.updatePayInfo(paymentModeDTO));
    }

    @ApiOperation(value = "启用禁用支付方式")
    @PostMapping("banPayInfo")
    public BaseResponse banPayInfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO) {
        paymentModeDTO.setModifier(this.getSysUserVO().getUsername());
        return ResultUtil.success(paymentModeService.banPayInfo(paymentModeDTO));
    }


}
