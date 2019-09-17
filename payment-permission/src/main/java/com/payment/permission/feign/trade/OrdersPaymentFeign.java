package com.payment.permission.feign.trade;
import cn.hutool.poi.excel.ExcelWriter;
import com.payment.common.dto.OrderPaymentDTO;
import com.payment.common.dto.OrderPaymentExportDTO;
import com.payment.common.dto.PayOutDTO;
import com.payment.common.entity.OrderPayment;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.trade.impl.OrdersPaymentFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "payment-trade", fallback = OrdersPaymentFeignImpl.class)
public interface OrdersPaymentFeign {


    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 分页查询汇款单
     **/
    @PostMapping(value = "/payOut/pageFindOrderPayment")
    BaseResponse pageFindOrderPayment(@RequestBody @ApiParam OrderPaymentDTO orderPaymentDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 查询汇款单详细信息
     **/
    @GetMapping(value = "/payOut/getOrderPaymentDetail")
    BaseResponse getOrderPaymentDetail(@RequestParam("orderPaymentId") @ApiParam String orderPaymentId,@RequestParam("language") @ApiParam String language);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 运维审核汇款单接口
     **/
    @GetMapping(value = "/payOut/operationsAudit")
    BaseResponse operationsAudit(@RequestParam("name") @ApiParam String name, @RequestParam("orderPaymentId") @ApiParam String orderPaymentId, @RequestParam("enabled") @ApiParam boolean enabled, @RequestParam("remark") @ApiParam String remark);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate
     **/
    @PostMapping(value = "/payOut/institutionPayment")
    BaseResponse institutionPayment(@RequestBody @ApiParam PayOutDTO payOutDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 商户后台审核汇款单接口
     **/
    @GetMapping(value = "/payOut/institutionAudit")
    BaseResponse institutionAudit(@RequestParam("name") @ApiParam String name, @RequestParam("orderPaymentId") @ApiParam String orderPaymentId, @RequestParam("enabled") @ApiParam boolean enabled, @RequestParam("remark") @ApiParam String remark);


    /**
     * @Author YangXu
     * @Date 2019/8/9
     * @Descripate 导出汇款单
     * @return
     **/
    @PostMapping("/payOut/exportOrderPayment")
    BaseResponse exportOrderPayment(OrderPaymentExportDTO orderPaymentDTO);


    /**
     * @Author YangXu
     * @Date 2019/8/12
     * @Descripate 人工汇款审核汇款单接口
     * @return
     **/
    @GetMapping(value = "/payOut/artificialPayOutAudit")
    BaseResponse artificialPayOutAudit(@RequestParam("name") @ApiParam String name, @RequestParam("orderPaymentId") @ApiParam String orderPaymentId, @RequestParam("enabled") @ApiParam boolean enabled, @RequestParam("remark") @ApiParam String remark);
}
