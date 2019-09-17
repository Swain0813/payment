package com.payment.permission.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.*;
import com.payment.common.entity.ReconciliationExport;
import com.payment.common.entity.SettleCheckAccount;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.common.vo.*;
import com.payment.permission.feign.reconciliation.ReconciliationFeign;
import com.payment.permission.service.InstitutionFeignService;
import com.payment.permission.service.OperationLogService;
import com.payment.permission.service.SysUserVoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @description: 调账管理记录
 * @author: YangXu
 * @create: 2019-03-29 14:24
 **/
@RestController
@Api(description = "账务管理接口")
@RequestMapping("/reconciliation")
public class ReconciliationFeignController extends BaseController {

    @Autowired
    private ReconciliationFeign reconciliationFeign;
    @Autowired
    private SysUserVoService sysUserVoService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private InstitutionFeignService institutionFeignService;

    @ApiOperation(value = "分页查询调账单")
    @PostMapping("/pageReconciliation")
    public BaseResponse pageReconciliation(@RequestBody @ApiParam ReconciliationDTO reconciliationDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(reconciliationDTO),
                "分页查询调账单"));
        return reconciliationFeign.pageReconciliation(reconciliationDTO);
    }

    @ApiOperation(value = "查询审核")
    @PostMapping("/pageReviewReconciliation")
    public BaseResponse pageReviewReconciliation(@RequestBody @ApiParam ReconciliationDTO reconciliationDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(reconciliationDTO),
                "分页查询审核调账单"));
        return reconciliationFeign.pageReviewReconciliation(reconciliationDTO);
    }

    @ApiOperation(value = "差错处理")
    @GetMapping("updateCheckAccount")
    public BaseResponse updateCheckAccount(@RequestParam @ApiParam String checkAccountId
            , @RequestParam(required = false) @ApiParam String remark) {
        return reconciliationFeign.updateCheckAccount(checkAccountId, remark);
    }

    @ApiOperation(value = "差错复核")
    @GetMapping("auditCheckAccount")
    public BaseResponse auditCheckAccount(@RequestParam @ApiParam String checkAccountId
            , @RequestParam @ApiParam Boolean enable, @RequestParam(required = false) @ApiParam String remark) {
        return reconciliationFeign.auditCheckAccount(checkAccountId, enable, remark);
    }

    @ApiOperation(value = "调账操作")
    @PostMapping("/doReconciliation")
    public BaseResponse doReconciliation(@RequestBody @ApiParam @Valid ReconOperDTO reconOperDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(reconOperDTO),
                "调账操作"));
        return reconciliationFeign.doReconciliation(reconOperDTO);
    }

    @ApiOperation(value = "调账审核")
    @GetMapping("/auditReconciliation")
    public BaseResponse auditReconciliation(@RequestParam @ApiParam String reconciliationId, @RequestParam @ApiParam boolean enabled,
                                            @RequestParam(required = false) @ApiParam String remark, @RequestParam @ApiParam String tradePwd) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "调账操作"));
        SysUserVO sysUserVO = this.getSysUserVO();
        if (!sysUserVoService.checkPassword(sysUserVoService.decryptPassword(tradePwd), sysUserVO.getTradePassword())) {
            throw new BusinessException(EResultEnum.TRADE_PASSWORD_ERROR.getCode());
        }
        return reconciliationFeign.auditReconciliation(reconciliationId, enabled, remark);
    }


    @ApiOperation(value = "分页查询对账管理")
    @PostMapping("/pageAccountCheckLog")
    public BaseResponse pageAccountCheckLog(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(searchAccountCheckDTO),
                "分页查询对账管理"));
        return reconciliationFeign.pageAccountCheckLog(searchAccountCheckDTO);
    }

    @ApiOperation(value = "分页查询对账管理详情")
    @PostMapping("/pageAccountCheck")
    public BaseResponse pageAccountCheck(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(searchAccountCheckDTO),
                "分页查询对账管理详情"));
        return reconciliationFeign.pageAccountCheck(searchAccountCheckDTO);
    }

    @ApiOperation(value = "分页查询对账管理复核详情")
    @PostMapping("/pageAccountCheckAudit")
    public BaseResponse pageAccountCheckAudit(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(searchAccountCheckDTO),
                "分页查询对账管理复核详情"));
        return reconciliationFeign.pageAccountCheckAudit(searchAccountCheckDTO);
    }

    @ApiOperation(value = "分页查询机构结算对账")
    @PostMapping("pageSettleAccountCheck")
    public BaseResponse pageSettleAccountCheck(@RequestBody @ApiParam TradeCheckAccountDTO tradeCheckAccountDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(tradeCheckAccountDTO),
                "分页查询机构结算对账"));
        return reconciliationFeign.pageSettleAccountCheck(tradeCheckAccountDTO);
    }

    @ApiOperation(value = "分页查询机构结算对账详情")
    @PostMapping("pageSettleAccountCheckDetail")
    public BaseResponse pageSettleAccountCheckDetail(@RequestBody @ApiParam TradeCheckAccountDTO tradeCheckAccountDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(tradeCheckAccountDTO),
                "分页查询机构结算对账详情"));
        return reconciliationFeign.pageSettleAccountCheckDetail(tradeCheckAccountDTO);
    }

    /**
     * 差错处理一览导出
     * @param searchAccountCheckDTO
     * @return
     */
    @ApiOperation(value = "导出对账管理详情")
    @PostMapping("/exportAccountCheck")
    public BaseResponse exportAccountCheck(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(searchAccountCheckDTO),
                "导出对账管理详情"));
        BaseResponse baseResponse = reconciliationFeign.exportAccountCheck(searchAccountCheckDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<CheckAccountVO> checkAccountVOS = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            checkAccountVOS.add(JSON.parseObject(JSON.toJSONString(datum), CheckAccountVO.class));
        }
        ExcelWriter writer = null;
        try {
            writer = institutionFeignService.getCheckAccountWriter(checkAccountVOS, CheckAccountVO.class);
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

    /**
     * 差错复核一览页面导出
     * @param searchAccountCheckDTO
     * @return
     */
    @ApiOperation(value = "导出对账管理复核详情")
    @PostMapping("/exportAccountCheckAudit")
    public BaseResponse exportAccountCheckAudit(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(searchAccountCheckDTO),
                "导出对账管理复核详情"));
        BaseResponse baseResponse = reconciliationFeign.exportAccountCheckAudit(searchAccountCheckDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<CheckAccountAuditVO> checkAccountVOS = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            checkAccountVOS.add(JSON.parseObject(JSON.toJSONString(datum), CheckAccountAuditVO.class));
        }
        ExcelWriter writer = null;
        try {
            writer = institutionFeignService.getCheckAccountAuditWriter(checkAccountVOS, CheckAccountAuditVO.class);
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

    /**
     * 机构结算对账导出
     * 机构结算表下载
     * @param tradeCheckAccountDTO
     * @return
     */
    @ApiOperation(value = "导出机构结算对账单")
    @PostMapping("/exportSettleAccountCheck")
    public BaseResponse exportSettleAccountCheck(@RequestBody @ApiParam TradeCheckAccountSettleExportDTO tradeCheckAccountDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(tradeCheckAccountDTO),
                "导出机构结算对账单"));
        BaseResponse baseResponse = reconciliationFeign.exportSettleAccountCheck(tradeCheckAccountDTO);
        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) baseResponse.getData();
        if (map == null || map.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ExcelWriter writer = ExcelUtil.getBigWriter();
        try {
            for (String key : map.keySet()) {
                if (key.equals("Statement")) {
                    writer.renameSheet(key);
                    List<SettleCheckAccount> settleCheckAccounts = JSON.parseArray(JSON.toJSONString(map.get(key)), SettleCheckAccount.class);
                    if(AsianWalletConstant.EN_US.equals(this.getLanguage())){//英文版的场合
                        writer = institutionFeignService.getSettleCheckAccountsWriter(writer,AsianWalletConstant.EN_US, settleCheckAccounts, SettleCheckAccountEnVO.class);
                    }else {
                        writer = institutionFeignService.getSettleCheckAccountsWriter(writer,  AsianWalletConstant.ZH_CN,settleCheckAccounts, SettleCheckAccount.class);
                    }
                } else {
                    writer.setSheet(key);
                    List<ExportSettleCheckAccountDetailVO> settleCheckAccountDetails = JSON.parseArray(JSON.toJSONString(map.get(key)), ExportSettleCheckAccountDetailVO.class);
                    if(AsianWalletConstant.EN_US.equals(this.getLanguage())) {//英文版的场合
                        writer = institutionFeignService.getSettleCheckAccountDetailWriter(writer, settleCheckAccountDetails, ExportSettleCheckAccountDetailEnVO.class);
                    }else {
                        writer = institutionFeignService.getSettleCheckAccountDetailWriter(writer,settleCheckAccountDetails, ExportSettleCheckAccountDetailVO.class);
                    }
                }
            }
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

    /**
     * 机构结算对账导出
     *
     * @param reconciliationDTO
     * @return
     */
    @ApiOperation(value = "导出机构资金变动详情")
    @PostMapping("/exportReconciliation")
    public BaseResponse exportReconciliation(@RequestBody @ApiParam ReconciliationExportDTO reconciliationDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(reconciliationDTO),
                "导出机构资金变动详情"));
        BaseResponse baseResponse = reconciliationFeign.exportReconciliation(reconciliationDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<ReconciliationExport> dtos = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            dtos.add(JSON.parseObject(JSON.toJSONString(datum), ReconciliationExport.class));
        }
        ExcelWriter writer = null;
        try {
            writer = institutionFeignService.getExportReconciliationWriter(dtos, ReconciliationExport.class);
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

    @ApiOperation(value = "查询可用余额")
    @PostMapping("/getAvailableBalance")
    public BaseResponse getAvailableBalance(@RequestBody @ApiParam @Valid SearchAvaBalDTO searchAvaBalDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(searchAvaBalDTO),
                "查询可用余额"));
        return reconciliationFeign.getAvailableBalance(searchAvaBalDTO);
    }
}
