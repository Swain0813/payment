package com.payment.permission.feign.reconciliation;

import com.payment.common.dto.*;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.reconciliation.Impl.ReconciliationFeignImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@FeignClient(value = "payment-finance", fallback = ReconciliationFeignImpl.class)
public interface ReconciliationFeign {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/25
     * @Descripate 分页查询调账单
     **/
    @PostMapping("/reconciliation/pageReconciliation")
    BaseResponse pageReconciliation(@RequestBody @ApiParam ReconciliationDTO reconciliationDTO);

    /**
     * 查询审核
     *
     * @param reconciliationDTO
     * @return
     */
    @PostMapping("/reconciliation/pageReviewReconciliation")
    BaseResponse pageReviewReconciliation(@RequestBody @ApiParam ReconciliationDTO reconciliationDTO);


    @ApiOperation(value = "导出查询调账单")
    @PostMapping("/reconciliation/exportReconciliation")
    BaseResponse exportReconciliation(@RequestBody @ApiParam ReconciliationExportDTO reconciliationDTO);

    /**
     * 差错处理
     *
     * @param
     * @return
     */
    @GetMapping("/finance/updateCheckAccount")
    BaseResponse updateCheckAccount(@RequestParam("checkAccountId") @ApiParam String checkAccountId
            , @RequestParam("remark") @ApiParam String remark);

    /**
     * 补单处理
     *
     * @param
     * @return
     */
    @GetMapping("/finance/auditCheckAccount")
    BaseResponse auditCheckAccount(@RequestParam("checkAccountId") @ApiParam String checkAccountId, @RequestParam("enable") @ApiParam Boolean enable
            , @RequestParam("remark") @ApiParam String remark);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/25
     * @Descripate 调账操作
     **/
    @PostMapping("/reconciliation/doReconciliation")
    BaseResponse doReconciliation(@RequestBody @ApiParam ReconOperDTO reconOperDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/25
     * @Descripate 调账审核
     **/
    @GetMapping("/reconciliation/auditReconciliation")
    BaseResponse auditReconciliation(@RequestParam("reconciliationId") @ApiParam String reconciliationId,
                                     @RequestParam("enabled") @ApiParam boolean enabled, @RequestParam("remark") @ApiParam String remark);

    /**
     * @param searchAccountCheckDTO 分页查询对账管理
     * @return
     */
    @PostMapping("/finance/pageAccountCheckLog")
    BaseResponse pageAccountCheckLog(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO);

    /**
     * @param searchAccountCheckDTO 分页查询对账管理详情
     * @return
     */
    @PostMapping("/finance/pageAccountCheck")
    BaseResponse pageAccountCheck(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO);

    /**
     * @param searchAccountCheckDTO 分页查询对账管理复核详情
     * @return
     */
    @PostMapping("/finance/pageAccountCheckAudit")
    BaseResponse pageAccountCheckAudit(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO);

    /**
     * @param tradeCheckAccountDTO 分页查询机构结算对账
     * @return
     */
    @PostMapping("/finance/pageSettleAccountCheck")
    BaseResponse pageSettleAccountCheck(@RequestBody @ApiParam TradeCheckAccountDTO tradeCheckAccountDTO);

    /**
     * @param tradeCheckAccountDTO 分页查询机构结算对账详情
     * @return
     */
    @PostMapping("/finance/pageSettleAccountCheckDetail")
    BaseResponse pageSettleAccountCheckDetail(@RequestBody @ApiParam TradeCheckAccountDTO tradeCheckAccountDTO);

    /**
     * @param searchAccountCheckDTO 导出对账管理详情
     * @return
     */
    @PostMapping("/finance/exportAccountCheck")
    BaseResponse exportAccountCheck(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO);

    /**
     * @param searchAccountCheckDTO 导出对账管理复核详情
     * @return
     */
    @PostMapping("/finance/exportAccountCheckAudit")
    BaseResponse exportAccountCheckAudit(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO);

    /**
     * @param tradeCheckAccountDTO 导出机构结算对账单
     * @return
     */
    @PostMapping("/finance/exportSettleAccountCheck")
    BaseResponse exportSettleAccountCheck(@RequestBody @ApiParam TradeCheckAccountSettleExportDTO tradeCheckAccountDTO);

    @ApiOperation(value = "查询可用余额")
    @PostMapping("/reconciliation/getAvailableBalance")
    BaseResponse getAvailableBalance(@RequestBody @ApiParam @Valid SearchAvaBalDTO searchAvaBalDTO);
}
