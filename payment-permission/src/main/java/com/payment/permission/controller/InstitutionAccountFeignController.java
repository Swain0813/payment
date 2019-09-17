package com.payment.permission.controller;

import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.TradeCheckAccountDTO;
import com.payment.common.dto.TradeCheckAccountExportDTO;
import com.payment.common.entity.TradeCheckAccount;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.common.utils.ArrayUtil;
import com.payment.common.vo.ExportTradeAccountVO;
import com.payment.common.vo.TradeCheckAccountDetailEnVO;
import com.payment.common.vo.TradeCheckAccountDetailVO;
import com.payment.common.vo.TradeCheckAccountEnVO;
import com.payment.permission.feign.finance.InstitutionAccountFeign;
import com.payment.permission.service.InstitutionAccountFeignService;
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

/**
 * @description: 机构账户接口
 * @author: XuWenQi
 * @create: 2019-04-15 11:47
 **/
@RestController
@Api(description = "机构账户接口")
@RequestMapping("/finance")
public class InstitutionAccountFeignController extends BaseController {

    @Autowired
    private InstitutionAccountFeign institutionAccountFeign;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private InstitutionAccountFeignService institutionAccountFeignService;

    @ApiOperation(value = "分页查询交易对账总表信息")
    @PostMapping("pageTradeCheckAccount")
    public BaseResponse pageTradeCheckAccount(@RequestBody @ApiParam TradeCheckAccountDTO tradeCheckAccountDTO) {
        return ResultUtil.success(institutionAccountFeign.pageTradeCheckAccount(tradeCheckAccountDTO));
    }

    /**
     * 机构交易对账信息导出
     *机构对账表下载
     * @param tradeCheckAccountDTO
     * @return
     */
    @ApiOperation(value = "导出机构交易对账信息")
    @PostMapping("exportTradeCheckAccount")
    public BaseResponse exportTradeCheckAccount(@RequestBody @ApiParam @Valid TradeCheckAccountExportDTO tradeCheckAccountDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(tradeCheckAccountDTO),
                "导出机构交易对账信息"));
        ExportTradeAccountVO exportTradeAccountVO = institutionAccountFeign.exportTradeCheckAccount(tradeCheckAccountDTO);
        //数据不存在的场合
        if (exportTradeAccountVO == null || ArrayUtil.isEmpty(exportTradeAccountVO.getTradeCheckAccounts()) || ArrayUtil.isEmpty(exportTradeAccountVO.getTradeAccountDetailVOS())) {
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ExcelWriter writer = null;
        try {
            if (AsianWalletConstant.EN_US.equals(this.getLanguage())) {
                //英文的场合
                writer = institutionAccountFeignService.getExcelWriter(exportTradeAccountVO, this.getLanguage(), TradeCheckAccountEnVO.class, TradeCheckAccountDetailEnVO.class);
            } else {
                //中文的场合
                writer = institutionAccountFeignService.getExcelWriter(exportTradeAccountVO, this.getLanguage(), TradeCheckAccount.class, TradeCheckAccountDetailVO.class);
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
}
