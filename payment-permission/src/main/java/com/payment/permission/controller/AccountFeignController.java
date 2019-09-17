package com.payment.permission.controller;

import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.*;
import com.payment.common.entity.TmMerChTvAcctBalance;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.common.vo.*;
import com.payment.permission.feign.institution.InstitutionFeign;
import com.payment.permission.service.InstitutionFeignService;
import com.payment.permission.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @description: 账户详情
 * @author: YangXu
 * @create: 2019-03-22 15:47
 **/
@RestController
@Api(description = "账户详情管理接口")
@RequestMapping("/account")
public class AccountFeignController extends BaseController {

    @Autowired
    private InstitutionFeign institutionFeign;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private InstitutionFeignService institutionFeignService;


    @ApiOperation(value = "分页查询机构账户信息列表")
    @PostMapping("/pageFindAccount")
    public BaseResponse pageFindAccount(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(accountSearchDTO),
                "分页查询机构账户信息列表"));
        return institutionFeign.pageFindAccount(accountSearchDTO);
    }

    @ApiOperation(value = "查询冻结余额流水详情")
    @PostMapping("/pageFrozenLogs")
    public BaseResponse pageFrozenLogs(@RequestBody @ApiParam FrozenMarginInfoDTO accountSearchDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(accountSearchDTO),
                "查询冻结余额流水详情"));
        return institutionFeign.pageFrozenLogs(accountSearchDTO);
    }

    @ApiOperation(value = "查询结算户余额流水详情")
    @PostMapping("/pageSettleLogs")
    public BaseResponse pageSettleLogs(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(accountSearchDTO),
                "查询结算户余额流水详情"));
        return institutionFeign.pageSettleLogs(accountSearchDTO);
    }

    @ApiOperation(value = "查询清算户余额流水详情")
    @PostMapping("/pageClearLogs")
    public BaseResponse pageClearLogs(@RequestBody @ApiParam ClearSearchDTO clearSearchDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(clearSearchDTO),
                "查询清算户余额流水详情"));
        return institutionFeign.pageClearLogs(clearSearchDTO);
    }

    @ApiOperation(value = "导出机构账户信息列表")
    @PostMapping("/exportAccountList")
    public BaseResponse exportAccountList(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(accountSearchDTO),
                "导出机构账户信息列表"));
        BaseResponse baseResponse = institutionFeign.exportAccountList(accountSearchDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<AccountListVO> accountList = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            accountList.add(JSON.parseObject(JSON.toJSONString(datum), AccountListVO.class));
        }
        ExcelWriter writer = null;
        try {
            writer = institutionFeignService.getAccountExcelWriter(accountList, AccountListVO.class);
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }

    @ApiOperation(value = "导出冻结余额流水详情")
    @PostMapping("/exportFrozenLogs")
    public BaseResponse exportFrozenLogs(@RequestBody @ApiParam FrozenMarginInfoDTO frozenMarginInfoDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(frozenMarginInfoDTO),
                "导出冻结余额流水详情"));
        BaseResponse baseResponse = institutionFeign.exportFrozenLogs(frozenMarginInfoDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<FrozenMarginInfoVO> frozenLogs = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            frozenLogs.add(JSON.parseObject(JSON.toJSONString(datum), FrozenMarginInfoVO.class));
        }
        ExcelWriter writer = null;
        try {
            writer = institutionFeignService.getFrozenLogsWriter(frozenLogs, FrozenMarginInfoVO.class);
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }

        return ResultUtil.success();
    }

    @ApiOperation(value = "导出结算户余额流水详情")
    @PostMapping("/exportSettleLogs")
    public BaseResponse exportSettleLogs(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(accountSearchDTO),
                "导出冻结余额流水详情"));
        BaseResponse baseResponse = institutionFeign.exportSettleLogs(accountSearchDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<TmMerChTvAcctBalance> tmMerChTvAcctBalance = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            tmMerChTvAcctBalance.add(JSON.parseObject(JSON.toJSONString(datum), TmMerChTvAcctBalance.class));
        }
        ExcelWriter writer = null;
        try {
            writer = institutionFeignService.getTmMerChTvAcctBalanceWriter(tmMerChTvAcctBalance, TmMerChTvAcctBalanceVO.class);
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }

        return ResultUtil.success();
    }

    @ApiOperation(value = "导出清算户余额流水详情")
    @PostMapping("/exportClearLogs")
    public BaseResponse exportClearLogs(@RequestBody @ApiParam ClearSearchDTO clearSearchDTO, HttpServletResponse response) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(clearSearchDTO),
                "导出清算户余额流水详情"));
        List<ClearAccountVO> clearAccountVOS = institutionFeign.exportClearLogs(clearSearchDTO);
        if (clearAccountVOS.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ExcelWriter writer = null;
        try {
            writer = institutionFeignService.getClearBalanceWriter(clearAccountVOS, ClearAccountVO.class);
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }

    @ApiOperation(value = "修改账户自动结算结算开关 最小起结金额")
    @PostMapping("/updateAccountSettle")
    public BaseResponse updateAccountSettle(@RequestBody @Valid @ApiParam AccountSettleDTO accountSettleDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(accountSettleDTO),
                "修改账户自动结算结算开关 最小起结金额"));
        return institutionFeign.updateAccountSettle(accountSettleDTO);
    }

    @ApiOperation(value = "分页查询代理机构账户信息列表")
    @PostMapping("/pageFindAgentAccount")
    public BaseResponse pageFindAgentAccount(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(accountSearchDTO),
                "分页查询代理机构账户信息列表"));
        return institutionFeign.pageFindAgentAccount(accountSearchDTO);
    }

    @ApiOperation(value = "导出代理机构账户信息")
    @PostMapping("/exportAgentAccount")
    public BaseResponse exportAgentAccount(@RequestBody @ApiParam AccountSearchExportDTO accountSearchDTO, HttpServletResponse response) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(accountSearchDTO),
                "导出代理机构账户信息"));
        final BaseResponse baseResponse = institutionFeign.exportAgentAccount(accountSearchDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<AgentAccountListVO> agentAccountListVOS = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            agentAccountListVOS.add(JSON.parseObject(JSON.toJSONString(datum), AgentAccountListVO.class));
        }
        ExcelWriter writer = null;
        try {
            if (AsianWalletConstant.EN_US.equals(this.getLanguage())) {
                //英文的场合
                writer = institutionFeignService.getAgentAccountWriter(agentAccountListVOS, AgentAccountExportENDTO.class);
            } else {
                //中文的场合
                writer = institutionFeignService.getAgentAccountWriter(agentAccountListVOS, AgentAccountExportDTO.class);
            }
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return ResultUtil.success();
    }
}
