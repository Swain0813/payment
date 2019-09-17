package com.payment.permission.feign.trade;
import com.payment.common.dto.RefundDTO;
import com.payment.common.dto.SearchOrderDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.trade.impl.RefundFeginImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "payment-trade", fallback = RefundFeginImpl.class)
public interface RefundFegin {


    /**
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 人工退款接口
     * @return
     **/
    @GetMapping(value = "/refund/artificialRefund")
    BaseResponse artificialRefund(@RequestParam("refundOrderId") @ApiParam String refundOrderId,@RequestParam("enabled") Boolean enabled,@RequestParam("remark") String remark);

    /**
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 分页查询退款接口
     * @return
     **/
    @PostMapping(value = "/refund/pageRefundOrder")
    BaseResponse pageRefundOrder(@RequestBody @ApiParam SearchOrderDTO searchOrderDTO);

    /**
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 分页查询退款接口
     * @return
     **/
    @PostMapping(value = "/refund/exportRefundOrder")
    BaseResponse exportRefundOrder(@RequestBody @ApiParam SearchOrderDTO searchOrderDTO);


    /**
     * 机构后台退款接口
     * @param refundDTO
     * @return
     */
    @PostMapping(value = "/refund/refundOrderSys")
    BaseResponse refundOrderSys(@RequestBody @ApiParam RefundDTO refundDTO);


}
