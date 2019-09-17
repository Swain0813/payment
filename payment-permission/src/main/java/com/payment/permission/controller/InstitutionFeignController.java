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
import com.payment.common.utils.ArrayUtil;
import com.payment.common.vo.AgencyInstitutionEnVO;
import com.payment.common.vo.AgencyInstitutionVO;
import com.payment.common.vo.ExportBankVO;
import com.payment.common.vo.InstitutionExportVO;
import com.payment.permission.entity.InstitutionExport;
import com.payment.permission.feign.institution.InstitutionFeign;
import com.payment.permission.service.InstitutionFeignService;
import com.payment.permission.service.OperationLogService;
import com.payment.permission.service.SysUserVoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-11 15:38
 **/
@RestController
@Api(description = "机构管理接口")
@RequestMapping("/institution")
@Slf4j
public class InstitutionFeignController extends BaseController {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private InstitutionFeign institutionFeign;

    @Autowired
    private SysUserVoService sysUserVoService;

    @Autowired
    private InstitutionFeignService institutionFeignService;

    @Value("${file.tmpfile}")
    private String tmpfile;//springboot启动的临时文件存放

    @ApiOperation(value = "添加机构信息")
    @PostMapping("/addInstitution")
    public BaseResponse addInstitution(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(institutionDTO),
                "添加机构信息"));
        institutionDTO.setPassword(sysUserVoService.encryptPassword("123456"));
        return institutionFeign.addInstitution(institutionDTO);
    }

    @ApiOperation(value = "修改机构信息")
    @PostMapping("/updateInstitution")
    public BaseResponse updateInstitution(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(institutionDTO),
                "修改机构信息"));
        return institutionFeign.updateInstitution(institutionDTO);
    }

    @ApiOperation(value = "分页查询机构信息列表")
    @PostMapping("/pageFindInstitution")
    public BaseResponse pageFindInstitution(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(institutionDTO),
                "分页查询机构信息列表"));
        return institutionFeign.pageFindInstitution(institutionDTO);
    }

    @ApiOperation(value = "分页查询机构审核信息列表")
    @PostMapping("/pageFindInstitutionAduit")
    public BaseResponse pageFindInstitutionAduit(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(institutionDTO),
                "分页查询机构审核信息列表"));
        return institutionFeign.pageFindInstitutionAduit(institutionDTO);
    }

    @ApiOperation(value = "分页查询机构历史记录信息列表")
    @PostMapping("/pageFindInstitutionHistory")
    public BaseResponse pageFindInstitutionHistory(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        return institutionFeign.pageFindInstitutionHistory(institutionDTO);
    }

    @ApiOperation(value = "根据机构Code查询机构信息详情")
    @GetMapping("/getInstitutionInfoByCode")
    public BaseResponse getInstitutionInfoByCode(@RequestParam @ApiParam String institutionCode) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "根据机构Code查询机构信息详情"));
        return institutionFeign.getInstitutionInfoByCode(institutionCode);
    }

    @ApiOperation(value = "根据机构Id查询机构信息详情")
    @GetMapping("/getInstitutionInfo")
    public BaseResponse getInstitutionInfo(@RequestParam @ApiParam String id) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "根据机构Id查询机构信息详情"));
        return institutionFeign.getInstitutionInfo(id);
    }

    @ApiOperation(value = "根据机构Id查询机构变更信息详情")
    @GetMapping("/getInstitutionHistoryInfo")
    public BaseResponse getInstitutionHistoryInfo(@RequestParam @ApiParam String id) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "根据机构Id查询机构变更信息详情"));
        return institutionFeign.getInstitutionHistoryInfo(id);
    }

    @ApiOperation(value = "根据机构Id查询机构审核信息详情")
    @GetMapping("/getInstitutionInfoAudit")
    public BaseResponse getInstitutionInfoAudit(@RequestParam @ApiParam String id) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "根据机构Id查询机构审核信息详情"));
        return institutionFeign.getInstitutionInfoAudit(id);
    }

    @ApiOperation(value = "禁用启用机构")
    @GetMapping("/banInstitution")
    public BaseResponse banInstitution(@RequestParam @ApiParam String institutionId, Boolean enabled) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "禁用启用机构"));
        return institutionFeign.banInstitution(institutionId, enabled);
    }

    @ApiOperation(value = "审核机构信息接口")
    @GetMapping("/auditInstitution")
    public BaseResponse auditInstitution(@RequestParam @ApiParam String institutionId, Boolean enabled, @RequestParam(required = false) String remark) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "审核机构信息接口"));
        return institutionFeign.auditInstitution(institutionId, enabled, remark);
    }

    @ApiOperation(value = "添加银行卡信息")
    @PostMapping("/addBankCard")
    public BaseResponse addBankCard(@RequestBody @ApiParam List<BankCardDTO> bankCardDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSONArray.toJSONString(bankCardDTO),
                "添加银行卡信息"));
        return institutionFeign.addBankCard(bankCardDTO);
    }

    @ApiOperation(value = "修改银行卡信息")
    @PostMapping("/updateBankCard")
    public BaseResponse updateBankCard(@RequestBody @ApiParam BankCardDTO bankCardDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSONArray.toJSONString(bankCardDTO),
                "修改银行卡信息"));
        return institutionFeign.updateBankCard(bankCardDTO);
    }

    @ApiOperation(value = "分页查询银行卡")
    @PostMapping("/pageBankCard")
    public BaseResponse pageBankCard(@RequestBody @ApiParam BankCardSearchDTO bankCardSearchDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONArray.toJSONString(bankCardSearchDTO),
                "分页查询银行卡"));
        return institutionFeign.pageBankCard(bankCardSearchDTO);
    }

    @ApiOperation(value = "根据机构id查询银行卡")
    @GetMapping("/selectBankCardByInsId")
    public BaseResponse selectBankCardByInsId(@RequestParam @ApiParam String institutionId) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "根据机构id查询银行卡"));
        return institutionFeign.selectBankCardByInsId(institutionId);
    }

    @ApiOperation(value = "启用禁用银行卡")
    @GetMapping("/banBankCard")
    public BaseResponse banBankCard(@RequestParam @ApiParam String bankCardId, @RequestParam @ApiParam Boolean enabled) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "启用禁用银行卡"));
        return institutionFeign.banBankCard(bankCardId, enabled);
    }

    /**
     * 机构信息一览导出
     *
     * @param institutionDTO
     * @return
     */
    @ApiOperation(value = "机构信息导出接口", notes = "机构信息导出接口")
    @PostMapping(value = "/export")
    public BaseResponse exportInformation(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(institutionDTO),
                "机构信息导出"));
        BaseResponse baseResponse = institutionFeign.exportInformation(institutionDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<InstitutionExportVO> institutionExportVOS = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            institutionExportVOS.add(JSON.parseObject(JSON.toJSONString(datum), InstitutionExportVO.class));
        }
        ExcelWriter writer = null;
        try {
            writer = institutionFeignService.getExcelWriter(institutionExportVOS, InstitutionExport.class);
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


    @ApiOperation(value = "通过机构名称查询机构信息")
    @PostMapping(value = "/getInstitutionInfoByName")
    public BaseResponse getInstitutionInfoByName(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        return institutionFeign.getInstitutionInfoByName(institutionDTO);
    }

    @ApiOperation(value = "设置默认银行卡")
    @GetMapping("/defaultBankCard")
    public BaseResponse defaultBankCard(@RequestParam @ApiParam String bankCardId, @RequestParam @ApiParam Boolean defaultFlag) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "设置默认银行卡"));
        return institutionFeign.defaultBankCard(bankCardId, defaultFlag);
    }

    @ApiOperation(value = "导入银行对照信息")
    @PostMapping("/importBankIssureId")
    public BaseResponse importBankIssureId(@RequestParam("file") @ApiParam MultipartFile file) {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(tmpfile);//指定临时文件路径，这个路径可以随便写
        factory.createMultipartConfig();
        List<BankIssuerid> biList = institutionFeignService.importBankIssureId(file, this.getSysUserVO().getUsername());
        return institutionFeign.importBankIssureId(biList);
    }

    @ApiOperation(value = "导入银行信息")
    @PostMapping("/importBank")
    public BaseResponse importBank(@RequestParam("file") @ApiParam MultipartFile file) {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(tmpfile);//指定临时文件路径，这个路径可以随便写
        factory.createMultipartConfig();
        List<Bank> bankList = institutionFeignService.importBank(file, this.getSysUserVO().getUsername());
        return institutionFeign.importBank(bankList);
    }

    @ApiOperation(value = "代理商下拉框信息一览")
    @PostMapping("/getAgencyList")
    public BaseResponse getAgencyList(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        return institutionFeign.getAgencyList(institutionDTO);
    }

    @ApiOperation(value = "代理商商户信息查询")
    @PostMapping(value = "/getAgencyInstitution")
    public BaseResponse getAgencyInstitution(@RequestBody @ApiParam @Valid QueryAgencyInstitutionDTO queryAgencyInstitutionDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(queryAgencyInstitutionDTO),
                "代理商商户信息查询"));
        return institutionFeign.getAgencyInstitution(queryAgencyInstitutionDTO);
    }

    @ApiOperation(value = "代理商商户信息导出", notes = "代理商商户信息导出")
    @PostMapping(value = "/exportAgencyInstitution")
    public BaseResponse exportAgencyInstitution(@RequestBody @ApiParam @Valid ExportAgencyInstitutionDTO exportAgencyInstitutionDTO, HttpServletResponse response) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(exportAgencyInstitutionDTO),
                "代理商商户信息导出"));
        List<AgencyInstitutionVO> agencyInstitutionVOS = institutionFeign.exportAgencyInstitution(exportAgencyInstitutionDTO);
        if (ArrayUtil.isEmpty(agencyInstitutionVOS)) {
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ExcelWriter writer = null;
        try {
            if (AsianWalletConstant.EN_US.equals(this.getLanguage())) {
                //英文的场合
                writer = institutionFeignService.exportAgencyInstitution(agencyInstitutionVOS, AgencyInstitutionEnVO.class);
            } else {
                writer = institutionFeignService.exportAgencyInstitution(agencyInstitutionVOS, AgencyInstitutionVO.class);
            }
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            log.error("=====================导出异常=====================", e);
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }

    @ApiOperation(value = "导出银行信息", notes = "导出银行信息")
    @PostMapping(value = "/exportBank")
    public BaseResponse exportBank(@RequestBody @ApiParam ExportBankDTO exportBankDTO, HttpServletResponse response) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(exportBankDTO),
                "导出银行信息"));
        List<ExportBankVO> exportBankVOS = institutionFeign.exportBank(exportBankDTO);
        if (ArrayUtil.isEmpty(exportBankVOS)) {
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ExcelWriter writer = null;
        try {
            if (AsianWalletConstant.EN_US.equals(this.getLanguage())) {
                //英文的场合
                writer = institutionFeignService.exportBank(exportBankVOS, ExportBankVO.class);
            } else {
                writer = institutionFeignService.exportBank(exportBankVOS, ExportBankVO.class);
            }
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            log.error("=====================导出异常=====================", e);
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }

}
