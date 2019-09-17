package com.payment.permission.feign.trade;

import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.trade.impl.CashierFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 收银台接口
 */
@FeignClient(value = "payment-trade", fallback = CashierFeignImpl.class)
public interface CashierFeign {

    @GetMapping("/onlineacquire/cashier")
    BaseResponse cashier(@RequestParam("orderId") @ApiParam String orderId);

}
