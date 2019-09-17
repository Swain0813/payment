package com.payment.channels.controller;

import com.payment.channels.service.XenditService;
import com.payment.common.base.BaseController;
import com.payment.common.dto.xendit.XenditDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @description: xendit
 * @author: XuWenQi
 * @create: 2019-06-19 11:26
 **/
@RestController
@Api(description = "xendit通道")
@RequestMapping("/xendit")
public class XenditController extends BaseController {

    @Autowired
    private XenditService xenditService;

    @ApiOperation(value = "xendit收单接口")
    @PostMapping("xenditPay")
    public BaseResponse xenditPay(@RequestBody @ApiParam @Valid XenditDTO xenditDTO) {
        return xenditService.xenditPay(xenditDTO);
    }

    @ApiOperation(value = "创建一个虚拟账户")
    @GetMapping("creatVirtualAccounts")
    public BaseResponse creatVirtualAccounts(@RequestParam @ApiParam @Valid String bankCode,String apiKey,String bankName) {
        return ResultUtil.success(xenditService.creatVirtualAccounts(bankCode,apiKey,bankName));
    }

    //@ApiOperation(value = "xendit根据OrderId查询订单信息")
    //@GetMapping("getPayInfo")
    //public BaseResponse getPayInfo(@RequestParam @ApiParam @Valid String OrderId,@RequestParam @ApiParam @Valid String apiKey) {
    //    return xenditService.getPayInfo(OrderId,apiKey);
    //}
    //
    //@ApiOperation(value = "Xendit可用银行查询接口")
    //@GetMapping("xenditBanks")
    //public BaseResponse xenditBanks(@RequestParam @ApiParam @Valid String apiKey) {
    //    return ResultUtil.success(xenditService.xenditBanks(apiKey));
    //}

}
