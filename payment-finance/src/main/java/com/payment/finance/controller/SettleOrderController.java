package com.payment.finance.controller;

import com.payment.common.base.BaseController;
import com.payment.common.dto.ReviewSettleDTO;
import com.payment.common.dto.SettleOrderDTO;
import com.payment.common.dto.SettleOrderExportDTO;
import com.payment.common.dto.WithdrawalDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.finance.service.SettleOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 机构结算交易相关接口
 */
@RestController
@Api(description = "机构结算交易接口")
@RequestMapping("/finance")
public class SettleOrderController extends BaseController {

    @Autowired
    private SettleOrderService settleOrderService;


    @ApiOperation(value = "分页查询机构结算交易一览查询")
    @PostMapping("/pageSettleOrder")
    public BaseResponse pageSettleOrder(@RequestBody @ApiParam SettleOrderDTO settleOrderDTO) {
        return ResultUtil.success(settleOrderService.pageSettleOrder(settleOrderDTO));
    }


    @ApiOperation(value = "分页查询机构结算交易详情")
    @PostMapping("/pageSettleOrderDetail")
    public BaseResponse pageSettleOrderDetail(@RequestBody @ApiParam SettleOrderExportDTO settleOrderDTO) {
        return ResultUtil.success(settleOrderService.pageSettleOrderDetail(settleOrderDTO));
    }

    @ApiOperation(value = "机构结算审核导出")
    @PostMapping("/exportSettleOrder")
    public BaseResponse exportSettleOrder(@RequestBody @ApiParam SettleOrderDTO settleOrderDTO) {
        return ResultUtil.success(settleOrderService.exportSettleOrder(settleOrderDTO));
    }

    @ApiOperation(value = "机构结算查询一览详情")
    @PostMapping("/pageSettleOrderQuery")
    public BaseResponse pageSettleOrderQuery(@RequestBody @ApiParam SettleOrderExportDTO settleOrderDTO) {
        return ResultUtil.success(settleOrderService.pageSettleOrderQuery(settleOrderDTO));
    }

    @ApiOperation(value = "机构结算审核")
    @PostMapping("/reviewSettlement")
    public BaseResponse reviewSettlement(@RequestBody @ApiParam ReviewSettleDTO reviewSettleDTO) {
        reviewSettleDTO.setModifier(this.getSysUserVO().getUsername());
        return ResultUtil.success(settleOrderService.reviewSettlement(reviewSettleDTO));
    }

    @ApiOperation(value = "手动提款")
    @PostMapping("/withdrawal")
    public BaseResponse withdrawal(@RequestBody @ApiParam WithdrawalDTO withdrawalDTO) {
        settleOrderService.withdrawal(withdrawalDTO,this.getSysUserVO().getUsername());
        return ResultUtil.success();
    }

}
