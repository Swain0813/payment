package com.payment.permission.feign.finance;

import com.payment.common.dto.ReviewSettleDTO;
import com.payment.common.dto.SettleOrderDTO;
import com.payment.common.dto.SettleOrderExportDTO;
import com.payment.common.dto.WithdrawalDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.finance.impl.SettleOrderFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 机构结算交易
 */
@FeignClient(value = "asianwallet-finance", fallback = SettleOrderFeignImpl.class)
public interface SettleOrderFeign {

    /**
     * 机构结算交易分页一览查询
     *
     * @param settleOrderDTO
     * @return
     */
    @PostMapping("/finance/pageSettleOrder")
    BaseResponse pageSettleOrder(@RequestBody @ApiParam SettleOrderDTO settleOrderDTO);


    /**
     * 机构结算交易分页详情
     *
     * @param settleOrderDTO
     * @return
     */
    @PostMapping("/finance/pageSettleOrderDetail")
    BaseResponse pageSettleOrderDetail(@RequestBody @ApiParam SettleOrderDTO settleOrderDTO);

    /**
     * 导出详情
     *
     * @param settleOrderDTO
     * @return
     */
    @PostMapping("/finance/exportSettleOrder")
    BaseResponse exportSettleOrder(@RequestBody @ApiParam SettleOrderDTO settleOrderDTO);

    /**
     * 机构结算审核
     *
     * @param reviewSettleDTO
     * @return
     */
    @PostMapping("/finance/reviewSettlement")
    BaseResponse reviewSettlement(@RequestBody @ApiParam ReviewSettleDTO reviewSettleDTO);


    /**
     * 机构结算查询一览详情
     *
     * @param settleOrderDTO
     * @return
     */
    @PostMapping("/finance/pageSettleOrderQuery")
    BaseResponse pageSettleOrderQuery(@RequestBody @ApiParam SettleOrderExportDTO settleOrderDTO);

    /**
     * 手动跑批
     *
     * @param withdrawalDTO@return
     */
    @PostMapping("/finance/withdrawal")
    BaseResponse withdrawal(WithdrawalDTO withdrawalDTO);
}
