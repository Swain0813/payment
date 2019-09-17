package com.payment.institution.controller;

import com.payment.common.base.BaseController;
import com.payment.common.dto.ExchangeRateDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.institution.service.ExchangeRateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @description: 汇率接口
 * @author: XuWenQi
 * @create: 2019-01-22 16:40
 **/
@RestController
@Api(description = "汇率接口")
@RequestMapping("/exchangeRate")
public class ExchangeRateController extends BaseController {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @ApiOperation(value = "添加汇率信息")
    @PostMapping("addExchangeRate")
    public BaseResponse addExchangeRate(@RequestBody @ApiParam @Valid ExchangeRateDTO exchangeRateDTO) {
        return ResultUtil.success(exchangeRateService.addExchangeRate(exchangeRateDTO, this.getSysUserVO().getUsername()));
    }

    @ApiOperation(value = "禁用汇率信息")
    @GetMapping("banExchangeRate")
    public BaseResponse banExchangeRate(@RequestParam @ApiParam String id) {
        return ResultUtil.success(exchangeRateService.banExchangeRate(id, this.getSysUserVO().getUsername()));
    }

    @ApiOperation(value = "分页多条件查询汇率信息")
    @PostMapping("getByMultipleConditions")
    public BaseResponse getByMultipleConditions(@RequestBody @ApiParam ExchangeRateDTO exchangeRateDTO) {
        return ResultUtil.success(exchangeRateService.getByMultipleConditions(exchangeRateDTO));
    }

}


