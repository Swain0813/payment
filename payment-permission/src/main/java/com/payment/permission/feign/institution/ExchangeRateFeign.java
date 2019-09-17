package com.payment.permission.feign.institution;

import com.payment.common.dto.ExchangeRateDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.impl.ExchangeRateFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @description: 汇率接口Feign端
 * @author: XuWenQi
 * @create: 2019-01-28 16:31
 **/
@FeignClient(value = "payment-institution", fallback = ExchangeRateFeignImpl.class)
public interface ExchangeRateFeign {

    /**
     * 添加汇率信息
     *
     * @param exchangeRateDTO 汇率输入实体
     * @return 修改条数
     */
    @PostMapping("/exchangeRate/addExchangeRate")
    BaseResponse addExchangeRate(@RequestBody @ApiParam ExchangeRateDTO exchangeRateDTO);


    /**
     * 禁用汇率信息
     * @param id 汇率id
     * @return 禁用条数
     */
    @GetMapping("/exchangeRate/banExchangeRate")
    BaseResponse banExchangeRate(@RequestParam("id") @ApiParam String id);


    /**
     * 多条件查询汇率信息
     *
     * @param exchangeRateDTO 汇率输入实体
     * @return 汇率输出实体集合
     */
    @PostMapping("/exchangeRate/getByMultipleConditions")
    BaseResponse getByMultipleConditions(@RequestBody @ApiParam ExchangeRateDTO exchangeRateDTO);


}
