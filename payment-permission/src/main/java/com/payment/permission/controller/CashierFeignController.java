package com.payment.permission.controller;

import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.trade.CashierFeign;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收银台
 */
@Api(description = "收银台接口")
@RestController
@RequestMapping("/onlineacquire")
public class CashierFeignController {

    @Autowired
    private CashierFeign cashierFeign;

    @ApiOperation(value = "收银台")
    @GetMapping("/cashier")
    public BaseResponse cashier(@RequestParam("orderId") @ApiParam String orderId) {
        return cashierFeign.cashier(orderId);
    }

}
