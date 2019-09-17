package com.payment.channels.controller;

import com.payment.channels.service.MegaPayService;
import com.payment.common.base.BaseController;
import com.payment.common.dto.megapay.*;
import com.payment.common.response.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-05-30 15:47
 **/
@RestController
@Api(description = "MeGaPay")
@RequestMapping("/megaPay")
public class MeGaPayController extends BaseController {

    @Autowired
    private MegaPayService megaPayService;

    @ApiOperation(value = "megaPay—THB收单接口")
    @PostMapping("megaPayTHB")
    public BaseResponse megaPayTHB(@RequestBody @ApiParam @Valid MegaPayRequestDTO megaPayRequestDTO) {
        return megaPayService.megaPayTHB(megaPayRequestDTO);
    }

    @ApiOperation(value = "megaPay—IDR收单接口")
    @PostMapping("megaPayIDR")
    public BaseResponse megaPayIDR(@RequestBody @ApiParam @Valid MegaPayIDRRequestDTO megaPayIDRRequestDTO) {
        return megaPayService.megaPayIDR(megaPayIDRRequestDTO);
    }

    @ApiOperation(value = "nextPos收单接口")
    @PostMapping("nextPos")
    public BaseResponse nextPos(@RequestBody @ApiParam @Valid NextPosRequestDTO nextPosRequestDTO) throws Exception {
        return megaPayService.nextPos(nextPosRequestDTO);
    }

    @ApiOperation(value = "nextPos查询订单接口")
    @PostMapping("nextPosQuery")
    public BaseResponse nextPosQuery(@RequestBody @ApiParam NextPosQueryDTO nextPosQueryDTO) {
        return megaPayService.nextPosQuery(nextPosQueryDTO);
    }

    @ApiOperation(value = "nextPos退款接口")
    @PostMapping("nextPosRefund")
    public BaseResponse nextPosRefund(@RequestBody @ApiParam NextPosRefundDTO nextPosRefundDTO) {
        return megaPayService.nextPosRefund(nextPosRefundDTO);
    }

}
