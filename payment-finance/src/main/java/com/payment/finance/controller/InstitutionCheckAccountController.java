package com.payment.finance.controller;
import com.payment.common.dto.TradeCheckAccountDTO;
import com.payment.common.dto.TradeCheckAccountExportDTO;
import com.payment.common.dto.TradeCheckAccountSettleExportDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.vo.ExportTradeAccountVO;
import com.payment.finance.service.InstitutionCheckAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Date;

/**
 * @description: 机构对账接口
 * @author: XuWenQi
 * @create: 2019-04-10 17:40
 **/
@RestController
@Api(description = "机构对账接口")
@RequestMapping("/finance")
public class InstitutionCheckAccountController {

    @Autowired
    private InstitutionCheckAccountService institutionCheckAccountService;

    @ApiOperation(value = "机构交易对账")
    @PostMapping("tradeAccountCheck")
    public BaseResponse tradeAccountCheck() {
        return ResultUtil.success(institutionCheckAccountService.tradeAccountCheck());
    }

    @ApiOperation(value = "机构结算对账")
    @GetMapping("settleAccountCheck")
    public BaseResponse settleAccountCheck(@RequestParam @ApiParam Date time) {
        return ResultUtil.success(institutionCheckAccountService.settleAccountCheck(time));
    }

    @ApiOperation(value = "查询前一天所有结算记录")
    @GetMapping("selectTcsStFlow")
    public BaseResponse selectTcsStFlow(@RequestParam @ApiParam String time) {
        return ResultUtil.success(institutionCheckAccountService.settleAccountCheck(DateToolUtils.getDateByStr(time)));
    }

    @ApiOperation(value = "分页查询机构结算对账")
    @PostMapping("pageSettleAccountCheck")
    public BaseResponse pageSettleAccountCheck(@RequestBody @ApiParam TradeCheckAccountDTO tradeCheckAccountDTO) {
        return ResultUtil.success(institutionCheckAccountService.pageSettleAccountCheck(tradeCheckAccountDTO));
    }

    @ApiOperation(value = "分页查询机构结算对账详情")
    @PostMapping("pageSettleAccountCheckDetail")
    public BaseResponse pageSettleAccountCheckDetail(@RequestBody @ApiParam TradeCheckAccountDTO tradeCheckAccountDTO) {
        return ResultUtil.success(institutionCheckAccountService.pageSettleAccountCheckDetail(tradeCheckAccountDTO));
    }

    @ApiOperation(value = "导出机构结算对账单")
    @PostMapping("exportSettleAccountCheck")
    public BaseResponse exportSettleAccountCheck(@RequestBody @ApiParam TradeCheckAccountSettleExportDTO tradeCheckAccountDTO) {
        return ResultUtil.success(institutionCheckAccountService.exportSettleAccountCheck(tradeCheckAccountDTO));
    }

    @ApiOperation(value = "分页查询交易对账总表信息")
    @PostMapping("pageTradeCheckAccount")
    public BaseResponse pageTradeCheckAccount(@RequestBody @ApiParam TradeCheckAccountDTO tradeCheckAccountDTO) {
        return ResultUtil.success(institutionCheckAccountService.pageTradeCheckAccount(tradeCheckAccountDTO));
    }

    @ApiOperation(value = "导出交易对账总表信息")
    @PostMapping("exportTradeCheckAccount")
    public ExportTradeAccountVO exportTradeCheckAccount(@RequestBody @ApiParam @Valid TradeCheckAccountExportDTO tradeCheckAccountDTO) {
        return institutionCheckAccountService.exportTradeCheckAccount(tradeCheckAccountDTO);
    }
}
