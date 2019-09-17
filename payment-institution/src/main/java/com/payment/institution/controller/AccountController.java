package com.payment.institution.controller;

import com.payment.common.base.BaseController;
import com.payment.common.dto.*;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.common.vo.ClearAccountVO;
import com.payment.institution.service.AccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @description: 账户Controller
 * @author: YangXu
 * @create: 2019-03-05 15:59
 **/
@RestController
@Api(description = "机构账户管理接口")
@RequestMapping("/account")
public class AccountController extends BaseController {
    @Autowired
    private AccountService accountService;

    @ApiOperation(value = "分页查询机构账户信息列表")
    @PostMapping("/pageFindAccount")
    public BaseResponse pageFindAccount(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO) {
        return ResultUtil.success(accountService.pageFindAccount(accountSearchDTO));
    }

    @ApiOperation(value = "导出机构账户信息列表")
    @PostMapping("/exportAccountList")
    public BaseResponse exportAccountList(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO) {
        return ResultUtil.success(accountService.exportAccountList(accountSearchDTO));
    }

    @ApiOperation(value = "查询冻结余额流水详情")
    @PostMapping("/pageFrozenLogs")
    public BaseResponse pageFrozenLogs(@RequestBody @ApiParam FrozenMarginInfoDTO frozenMarginInfoDTO) {
        return ResultUtil.success(accountService.pageFrozenLogs(frozenMarginInfoDTO));
    }

    @ApiOperation(value = "查询结算户余额流水详情")
    @PostMapping("/pageSettleLogs")
    public BaseResponse pageSettleLogs(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO) {
        return ResultUtil.success(accountService.pageSettleLogs(accountSearchDTO));
    }

    @ApiOperation(value = "导出冻结余额流水详情")
    @PostMapping("/exportFrozenLogs")
    public BaseResponse exportFrozenLogs(@RequestBody @ApiParam FrozenMarginInfoDTO accountSearchDTO) {
        return ResultUtil.success(accountService.exportFrozenLogs(accountSearchDTO));
    }

    @ApiOperation(value = "导出结算户余额流水详情")
    @PostMapping("/exportSettleLogs")
    public BaseResponse exportleLogs(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO) {
        return ResultUtil.success(accountService.exportSettleLogs(accountSearchDTO));
    }

    @ApiOperation(value = "导出清算户余额流水详情")
    @PostMapping("/exportClearLogs")
    public List<ClearAccountVO> exportClearLogs(@RequestBody @ApiParam ClearSearchDTO clearSearchDTO) {
        return accountService.exportClearLogs(clearSearchDTO);
    }

    @ApiOperation(value = "查询清算户余额流水详情")
    @PostMapping("/pageClearLogs")
    public BaseResponse pageClearLogs(@RequestBody @ApiParam ClearSearchDTO clearSearchDTO) {
        return ResultUtil.success(accountService.pageClearLogs(clearSearchDTO));
    }

    @ApiOperation(value = "修改账户自动结算结算开关 最小起结金额")
    @PostMapping("/updateAccountSettle")
    public BaseResponse updateAccountSettle(@RequestBody @Valid @ApiParam AccountSettleDTO accountSettleDTO) {
        accountSettleDTO.setModifier(this.getSysUserVO().getUsername());
        return ResultUtil.success(accountService.updateAccountSettle(accountSettleDTO));
    }

    @ApiOperation(value = "分页查询代理商账户信息列表")
    @PostMapping("/pageFindAgentAccount")
    public BaseResponse pageFindAgentAccount(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO) {
        return ResultUtil.success(accountService.pageFindAgentAccount(accountSearchDTO));
    }

    @ApiOperation(value = "导出代理商账户")
    @PostMapping("/exportAgentAccount")
    public BaseResponse exportAgentAccount(@RequestBody @ApiParam AccountSearchExportDTO accountSearchDTO) {
        return ResultUtil.success(accountService.exportAgentAccount(accountSearchDTO));
    }
}
