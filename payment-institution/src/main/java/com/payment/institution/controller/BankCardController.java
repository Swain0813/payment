package com.payment.institution.controller;

import com.payment.common.base.BaseController;
import com.payment.common.dto.*;
import com.payment.common.entity.Bank;
import com.payment.common.entity.BankIssuerid;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.common.vo.ExportBankVO;
import com.payment.institution.service.BankCardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description: 银行卡管理
 * @author: YangXu
 * @create: 2019-02-27 17:58
 **/
@RestController
@Api(description = "银行卡管理接口")
@RequestMapping("/bankcard")
public class BankCardController extends BaseController {

    @Autowired
    private BankCardService bankCardService;

    @Value("${file.tmpfile}")
    private String tmpfile;//springboot启动的临时文件存放

    @ApiOperation(value = "添加银行卡信息")
    @PostMapping("/addBankCard")
    public BaseResponse addBankCard(@RequestBody @ApiParam List<BankCardDTO> bankCardDTO) {
        return ResultUtil.success(bankCardService.addBankCard(this.getSysUserVO().getUsername(), bankCardDTO));
    }

    @ApiOperation(value = "修改银行卡信息")
    @PostMapping("/updateBankCard")
    public BaseResponse updateBankCard(@RequestBody @ApiParam BankCardDTO bankCardDTO) {
        return ResultUtil.success(bankCardService.updateBankCard(this.getSysUserVO().getUsername(), bankCardDTO));
    }

    @ApiOperation(value = "根据机构id查询银行卡")
    @GetMapping("/selectBankCardByInsId")
    public BaseResponse selectBankCardByInsId(@RequestParam @ApiParam String institutionId) {
        return ResultUtil.success(bankCardService.selectBankCardByInsId(institutionId));
    }

    @ApiOperation(value = "分页查询银行卡")
    @PostMapping("/pageBankCard")
    public BaseResponse pageBankCard(@RequestBody @ApiParam BankCardSearchDTO bankCardSearchDTO) {
        return ResultUtil.success(bankCardService.pageBankCard(bankCardSearchDTO));
    }

    @ApiOperation(value = "启用禁用银行卡")
    @GetMapping("/banBankCard")
    public BaseResponse banBankCard(@RequestParam @ApiParam String bankCardId, @RequestParam @ApiParam Boolean enabled) {
        return ResultUtil.success(bankCardService.banBankCard(this.getSysUserVO().getUsername(), bankCardId, enabled));
    }

    @ApiOperation(value = "设置默认银行卡")
    @GetMapping("/defaultBankCard")
    public BaseResponse defaultBankCard(@RequestParam @ApiParam String bankCardId, @RequestParam @ApiParam Boolean defaultFlag) {
        return ResultUtil.success(bankCardService.defaultBankCard(this.getSysUserVO().getUsername(), bankCardId, defaultFlag));
    }

    /************************************ 银行 issureid **********************************************/

    @ApiOperation(value = "配置银行issureid对照信息")
    @PostMapping("/addBankIssureId")
    public BaseResponse addBankIssureId(@RequestBody @ApiParam List<BankIssuerid> bankIssuerid) {
        return ResultUtil.success(bankCardService.addBankIssureId(this.getSysUserVO().getUsername(), bankIssuerid));
    }

    @ApiOperation(value = "修改银行issureid对照信息")
    @PostMapping("/updateBankIssureId")
    public BaseResponse updateBankIssureId(@RequestBody @ApiParam BankIssuerid bankIssuerid) {
        return ResultUtil.success(bankCardService.updateBankIssureId(this.getSysUserVO().getUsername(), bankIssuerid));
    }

    @ApiOperation(value = "查询银行issureid对照信息")
    @PostMapping("/pageFindBankIssuerid")
    public BaseResponse pageFindBankIssuerid(@RequestBody @ApiParam BankIssueridDTO bankIssueridDTO) {
        return ResultUtil.success(bankCardService.pageFindBankIssuerid(bankIssueridDTO));
    }

    @ApiOperation(value = "导出银行issureid对照信息")
    @PostMapping("/exportBankIssuerid")
    public BaseResponse exportBankIssuerid(@RequestBody @ApiParam BankIssueridExportDTO bankIssueridDTO) {
        return ResultUtil.success(bankCardService.exportBankIssuerid(bankIssueridDTO));
    }

    @ApiOperation(value = "导入银行对照信息")
    @PostMapping("importBankIssureId")
    public BaseResponse importBankIssureId(@RequestBody @ApiParam List<BankIssuerid> bankIssuerid) {
        return ResultUtil.success(bankCardService.importBankIssureId(bankIssuerid));
    }

    /************************************ 银行 **********************************************/

    @ApiOperation(value = "配置银行对照信息")
    @PostMapping("/addBank")
    public BaseResponse addBank(@RequestBody @ApiParam Bank bank) {
        return ResultUtil.success(bankCardService.addBank(this.getSysUserVO().getUsername(), bank));
    }

    @ApiOperation(value = "修改银行信息")
    @PostMapping("/updateBank")
    public BaseResponse updateBank(@RequestBody @ApiParam Bank bank) {
        return ResultUtil.success(bankCardService.updateBank(this.getSysUserVO().getUsername(), bank));
    }

    @ApiOperation(value = "查询信息")
    @PostMapping("/pageFindBank")
    public BaseResponse pageFindBank(@RequestBody @ApiParam BankDTO bank) {
        return ResultUtil.success(bankCardService.pageFindBank(bank));
    }

    @ApiOperation(value = "导入银行信息")
    @PostMapping("/importBank")
    public BaseResponse importBank(@RequestBody @ApiParam List<Bank> banks) {
        return ResultUtil.success(bankCardService.importBank(banks));
    }

    @ApiOperation(value = "导出银行信息")
    @PostMapping("/exportBank")
    public List<ExportBankVO> exportBank(@RequestBody @ApiParam ExportBankDTO exportBankDTO) {
        return bankCardService.exportBank(exportBankDTO);
    }
}
