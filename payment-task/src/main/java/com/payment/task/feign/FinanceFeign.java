package com.payment.task.feign;
import com.payment.common.dto.ReconOperDTO;
import com.payment.common.response.BaseResponse;
import com.payment.task.feign.Impl.FinanceFeignImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import javax.validation.Valid;

@FeignClient(value = "payment-finance", fallback = FinanceFeignImpl.class)
public interface FinanceFeign {

    @ApiOperation(value = "机构结算对账")
    @GetMapping("/finance/selectTcsStFlow")
    BaseResponse selectTcsStFlow(@RequestParam("time") @ApiParam String time);

    @ApiOperation(value = "机构交易对账")
    @PostMapping("/finance/tradeAccountCheck")
    BaseResponse tradeAccountCheck();

    @ApiOperation(value = "结算跑批调调账操作")
    @PostMapping("/reconciliation/doReconciliationBatch")
    BaseResponse doReconciliationBatch(@RequestBody @ApiParam  @Valid ReconOperDTO reconOperDTO);

    @ApiOperation(value = "结算跑批调调账审核")
    @GetMapping("/reconciliation/auditReconciliationBatch")
    BaseResponse auditReconciliationBatch(@RequestParam("reconciliationId") @ApiParam String  reconciliationId, @RequestParam("enabled") @ApiParam boolean  enabled,@RequestParam("remark") @ApiParam String  remark);
}
