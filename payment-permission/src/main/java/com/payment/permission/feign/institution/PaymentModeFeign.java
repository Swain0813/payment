package com.payment.permission.feign.institution;


/**
 * 支付方式Feign 接口
 */

import com.payment.common.dto.PaymentModeDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.impl.PaymentModeFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "payment-institution", fallback = PaymentModeFeignImpl.class)
public interface PaymentModeFeign {

    /**
     * 添加支付方式的不同语言
     *
     * @param paymentModeDTO
     * @return
     */
    @PostMapping("/paymentmode/addOtherLanguage")
    BaseResponse addOtherLanguage(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO);

    /**
     * 添加支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @PostMapping("/paymentmode/addPayinfo")
    BaseResponse addPayinfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO);

    /**
     * 查询支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @PostMapping("/paymentmode/pagePayInfo")
    BaseResponse pagePayInfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO);


    /**
     * 更新支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @PostMapping("/paymentmode/updatePayInfo")
    BaseResponse updatePayInfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO);

    /**
     * 查询所有支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @PostMapping("/paymentmode/getPayInfo")
    BaseResponse getPayInfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO);

    /**
     * 禁用支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @PostMapping("/paymentmode/banPayInfo")
    BaseResponse banPayInfo(@RequestBody @ApiParam PaymentModeDTO paymentModeDTO);

}
