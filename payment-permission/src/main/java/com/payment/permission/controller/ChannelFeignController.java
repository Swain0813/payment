package com.payment.permission.controller;

import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.*;
import com.payment.common.entity.Bank;
import com.payment.common.entity.BankIssuerid;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.common.vo.BankIssueridExportVO;
import com.payment.common.vo.ChannelExportVO;
import com.payment.permission.feign.institution.ChannelFeign;
import com.payment.permission.service.ChannelFeignService;
import com.payment.permission.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @description: 通道Feign
 * @author: YangXu
 * @create: 2019-01-30 14:38
 **/
@Api(description = "通道管理接口")
@RestController
@RequestMapping("/channel")
public class ChannelFeignController extends BaseController {

    @Autowired
    private ChannelFeign channelFeign;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private ChannelFeignService channelFeignService;

    @ApiOperation(value = "添加通道信息")
    @PostMapping("/addChannel")
    public BaseResponse addChannel(@RequestBody @ApiParam ChannelDTO channelDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(channelDTO),
                "添加通道信息"));
        return channelFeign.addChannel(channelDTO);
    }

    @ApiOperation(value = "修改通道产信息")
    @PostMapping("/updateChannel")
    public BaseResponse updateChannel(@RequestBody @ApiParam ChannelDTO channelDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(channelDTO),
                "修改通道产品关联信息"));
        return channelFeign.updateChannel(channelDTO);
    }

    @ApiOperation(value = "分页查询通道信息")
    @PostMapping("/pageFindChannel")
    public BaseResponse pageFindChannel(@RequestBody @ApiParam ChannelDTO channelDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(channelDTO),
                "分页查询通道信息"));
        return channelFeign.pageFindChannel(channelDTO);
    }

    @ApiOperation(value = "查询所有通道信息")
    @GetMapping("/getAllChannel")
    public BaseResponse getAllChannel() {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, null,
                "查询所有通道信息"));
        return channelFeign.getAllChannel();
    }

    @ApiOperation(value = "分页查询产品通道管理信息")
    @PostMapping("/pageFindProductChannel")
    public BaseResponse pageFindProductChannel(@RequestBody @ApiParam SearchChannelDTO searchChannelDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(searchChannelDTO),
                "分页查询产品通道管理信息"));
        return channelFeign.pageFindProductChannel(searchChannelDTO);
    }

    @ApiOperation(value = "启用禁用通道")
    @GetMapping("/banChannel")
    public BaseResponse banChannel(@RequestParam @ApiParam @Valid String channelId, @RequestParam @ApiParam @Valid Boolean enabled) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "启用禁用通道"));
        return channelFeign.banChannel(channelId, enabled);
    }

    @ApiOperation(value = "根据通道id查取详情")
    @GetMapping("/getChannelById")
    public BaseResponse getChannelById(@RequestParam @ApiParam String channelId) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "根据通道id查取详情"));
        return channelFeign.getChannelById(channelId);
    }

    @ApiOperation(value = "根据产品id查取通道")
    @GetMapping("/getChannelByProductId")
    public BaseResponse getChannelByProductId(@RequestParam @ApiParam String productId) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "根据产品id查取通道"));
        return channelFeign.getChannelByProductId(productId);
    }

    @ApiOperation(value = "根据机构Id和产品Id查询未添加通道")
    @GetMapping("/getChannelByInsIdAndProId")
    public BaseResponse getChannelByInsIdAndProId(@RequestParam @ApiParam String institutionId, @RequestParam @ApiParam String productId) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "根据机构Id和产品Id查询未添加通道"));
        return channelFeign.getChannelByInsIdAndProId(institutionId, productId);
    }

    @ApiOperation(value = "配置银行issureid对照信息")
    @PostMapping("/addBankIssureId")
    public BaseResponse addBankIssureId(@RequestBody @ApiParam List<BankIssuerid> bankIssuerid) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSONArray.toJSONString(this.getRequest().getParameterMap()),
                "配置银行issureid对照信息"));
        return channelFeign.addBankIssureId(bankIssuerid);
    }

    @ApiOperation(value = "修改银行issureid对照信息")
    @PostMapping("/updateBankIssureId")
    public BaseResponse updateBankIssureId(@RequestBody @ApiParam BankIssuerid bankIssuerid) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "修改银行issureid对照信息"));
        return channelFeign.updateBankIssureId(bankIssuerid);
    }

    @ApiOperation(value = "查询银行issureid对照信息")
    @PostMapping("/pageFindBankIssuerid")
    public BaseResponse pageFindBankIssuerid(@RequestBody @ApiParam BankIssueridDTO bankIssueridDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "查询银行issureid对照信息"));
        return channelFeign.pageFindBankIssuerid(bankIssueridDTO);
    }

    @ApiOperation(value = "配置银行信息")
    @PostMapping("/addBank")
    public BaseResponse addBank(@RequestBody @ApiParam Bank bank) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "配置银行信息"));
        return channelFeign.addBank(bank);
    }

    @ApiOperation(value = "修改银行信息")
    @PostMapping("/updateBank")
    public BaseResponse updateBank(@RequestBody @ApiParam Bank bank) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "修改银行信息"));
        return channelFeign.updateBank(bank);
    }

    @ApiOperation(value = "查询银行信息")
    @PostMapping("/pageFindBank")
    public BaseResponse pageFindBank(@RequestBody @ApiParam BankDTO bank) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "查询银行信息"));
        return channelFeign.pageFindBank(bank);
    }

    @ApiOperation(value = "通道导出功能", notes = "通道导出功能")
    @PostMapping("/exportAllChannels")
    public BaseResponse exportAllOrders(@RequestBody @ApiParam ChannelDTO channelDTO, HttpServletResponse response) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(channelDTO),
                "通道导出功能"));
        BaseResponse baseResponse = channelFeign.exportAllChannels(channelDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<ChannelExportVO> channelExportVOs = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            channelExportVOs.add(JSON.parseObject(JSON.toJSONString(datum), ChannelExportVO.class));
        }
        ExcelWriter writer = null;
        try {
            writer = channelFeignService.getChannelsExcelWriter(channelExportVOs, ChannelExportVO.class);
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }

    @ApiOperation(value = "导出银行issureid对照信息")
    @PostMapping("/exportBankIssuerid")
    public BaseResponse exportBankIssuerid(@RequestBody @ApiParam BankIssueridExportDTO bankIssueridDTO, HttpServletResponse response) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(bankIssueridDTO),
                "导出银行issureid对照信息"));
        BaseResponse baseResponse = channelFeign.exportBankIssuerid(bankIssueridDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<BankIssuerid> bankIssuerids = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            bankIssuerids.add(JSON.parseObject(JSON.toJSONString(datum), BankIssuerid.class));
        }
        ExcelWriter writer = null;
        try {
            writer = channelFeignService.getBankIssuerWriter(bankIssuerids, BankIssueridExportVO.class);
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
