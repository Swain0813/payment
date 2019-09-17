package com.payment.finance.controller;
import com.payment.common.base.BaseController;
import com.payment.common.dto.ReconOperDTO;
import com.payment.common.dto.ReconciliationDTO;
import com.payment.common.dto.ReconciliationExportDTO;
import com.payment.common.dto.SearchAvaBalDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.finance.service.ReconciliationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * @description: 调账接口
 * @author: YangXu
 * @create: 2019-03-25 15:52
 **/
@RestController
@Api(description = "调账管理")
@RequestMapping("/reconciliation")
public class ReconciliationController extends BaseController {

    @Autowired
    private ReconciliationService reconciliationService;


    @ApiOperation(value = "分页查询调账单")
    @PostMapping("pageReconciliation")
    public BaseResponse pageReconciliation(@RequestBody @ApiParam ReconciliationDTO reconciliationDTO) {
        return ResultUtil.success(reconciliationService.pageReconciliation(reconciliationDTO));
    }

    @ApiOperation(value = "导出查询调账单")
    @PostMapping("exportReconciliation")
    public BaseResponse exportReconciliation(@RequestBody @ApiParam ReconciliationExportDTO reconciliationDTO) {
        return ResultUtil.success(reconciliationService.exportReconciliation(reconciliationDTO));
    }

    @ApiOperation(value = "分页查询审核调账单")
    @PostMapping("pageReviewReconciliation")
    public BaseResponse pageReviewReconciliation(@RequestBody @ApiParam ReconciliationDTO reconciliationDTO) {
        return ResultUtil.success(reconciliationService.pageReviewReconciliation(reconciliationDTO));
    }

    @ApiOperation(value = "资金变动操作")
    @PostMapping("doReconciliation")
    public BaseResponse doReconciliation(@RequestBody @ApiParam  @Valid ReconOperDTO reconOperDTO) {
        return ResultUtil.success(reconciliationService.doReconciliation(this.getSysUserVO().getUsername(),reconOperDTO));
    }

    @ApiOperation(value = "资金变动审核")
    @GetMapping("auditReconciliation")
    public BaseResponse auditReconciliation(@RequestParam @ApiParam String reconciliationId, @RequestParam @ApiParam boolean enabled
            , @RequestParam(required = false) @ApiParam String  remark) {
        return ResultUtil.success(reconciliationService.auditReconciliation(this.getSysUserVO().getUsername(),reconciliationId,enabled,remark));
    }


    @ApiOperation(value = "结算跑批调调账操作")
    @PostMapping("/doReconciliationBatch")
    public BaseResponse doReconciliationBatch(@RequestBody @ApiParam  @Valid ReconOperDTO reconOperDTO) {
        return ResultUtil.success(reconciliationService.doReconciliation("定时跑批生成结算交易",reconOperDTO));
    }

    @ApiOperation(value = "结算跑批调调账审核")
    @GetMapping("/auditReconciliationBatch")
    public BaseResponse auditReconciliationBatch(@RequestParam @ApiParam String  reconciliationId,@RequestParam @ApiParam boolean  enabled
            ,@RequestParam(required = false) @ApiParam String  remark) {
        return ResultUtil.success(reconciliationService.auditReconciliation("定时跑批生成结算交易",reconciliationId,enabled,remark));
    }

    @ApiOperation(value = "查询可用余额")
    @PostMapping("/getAvailableBalance")
    public BaseResponse getAvailableBalance(@RequestBody @ApiParam @Valid SearchAvaBalDTO searchAvaBalDTO) {
        return ResultUtil.success(reconciliationService.getAvailableBalance(searchAvaBalDTO));
    }
}
