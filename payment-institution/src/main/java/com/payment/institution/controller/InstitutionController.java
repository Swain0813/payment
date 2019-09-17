package com.payment.institution.controller;

import com.payment.common.base.BaseController;
import com.payment.common.dto.ExportAgencyInstitutionDTO;
import com.payment.common.dto.InstitutionDTO;
import com.payment.common.dto.InstitutionExportDTO;
import com.payment.common.dto.QueryAgencyInstitutionDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.common.vo.AgencyInstitutionVO;
import com.payment.common.vo.InstitutionExportVO;
import com.payment.institution.service.InstitutionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @description: 机构Controller
 * @author: YangXu
 * @create: 2019-01-25 11:07
 **/
@RestController
@Api(description = "机构接口")
@RequestMapping("/institution")
public class InstitutionController extends BaseController {


    @Autowired
    private InstitutionService institutionService;

    @ApiOperation(value = "添加机构信息")
    @PostMapping("/addInstitution")
    public BaseResponse addInstitution(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        return ResultUtil.success(institutionService.addInstitution(this.getSysUserVO(), institutionDTO));
    }

    @ApiOperation(value = "修改机构信息")
    @PostMapping("/updateInstitution")
    public BaseResponse updateInstitution(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        return ResultUtil.success(institutionService.updateInstitution(this.getSysUserVO(), institutionDTO));
    }

    @ApiOperation(value = "分页查询机构信息列表")
    @PostMapping("/pageFindInstitution")
    public BaseResponse pageFindInstitution(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        return ResultUtil.success(institutionService.pageFindInstitution(institutionDTO));
    }

    @ApiOperation(value = "分页查询机构历史记录信息列表")
    @PostMapping("/pageFindInstitutionHistory")
    public BaseResponse pageFindInstitutionHistory(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        return ResultUtil.success(institutionService.pageFindInstitutionHistory(institutionDTO));
    }

    @ApiOperation(value = "分页查询机构审核信息列表")
    @PostMapping("/pageFindInstitutionAduit")
    public BaseResponse pageFindInstitutionAduit(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        return ResultUtil.success(institutionService.pageFindInstitutionAduit(institutionDTO));
    }

    @ApiOperation(value = "根据机构Code查询机构信息详情")
    @GetMapping("/getInstitutionInfoByCode")
    public BaseResponse getInstitutionInfoByCode(@RequestParam(required = false) @ApiParam String institutionCode) {
        return ResultUtil.success(institutionService.getInstitutionInfoByCode(institutionCode));
    }
    @ApiOperation(value = "根据机构Id查询机构信息详情")
    @GetMapping("/getInstitutionInfo")
    public BaseResponse getInstitutionInfo(@RequestParam @ApiParam String id) {
        return ResultUtil.success(institutionService.getInstitutionInfo(id));
    }

    @ApiOperation(value = "根据机构Id查询机构变更信息详情")
    @GetMapping("/getInstitutionHistoryInfo")
    public BaseResponse getInstitutionHistoryInfo(@RequestParam @ApiParam String id) {
        return ResultUtil.success(institutionService.getInstitutionHistoryInfo(id));
    }

    @ApiOperation(value = "根据机构Id查询机构审核信息详情")
    @GetMapping("/getInstitutionInfoAudit")
    public BaseResponse getInstitutionInfoAudit(@RequestParam @ApiParam String id) {
        return ResultUtil.success(institutionService.getInstitutionInfoAudit(id));
    }

    @ApiOperation(value = "禁用启用机构")
    @GetMapping("/banInstitution")
    public BaseResponse banInstitution(@RequestParam @ApiParam String institutionId, @RequestParam @ApiParam Boolean enabled) {
        return ResultUtil.success(institutionService.banInstitution(this.getSysUserVO().getUsername(), institutionId, enabled));
    }

    @ApiOperation(value = "审核机构信息接口")
    @GetMapping("/auditInstitution")
    public BaseResponse auditInstitution(@RequestParam @ApiParam String institutionId, @RequestParam @ApiParam Boolean enabled, @RequestParam(required = false) @ApiParam String remark) {
        return ResultUtil.success(institutionService.auditInstitution(this.getSysUserVO().getUsername(), institutionId, enabled, remark));
    }

    @ApiOperation(value = "机构信息导出接口", notes = "机构信息导出接口")
    @PostMapping(value = "/export")
    public BaseResponse exportInformation(@RequestBody @ApiParam InstitutionExportDTO institutionDTO) {
        List<InstitutionExportVO> institutions = institutionService.exportInformation(institutionDTO);
        return ResultUtil.success(institutions);
    }

    @ApiOperation(value = "通过机构名称查询机构信息")
    @PostMapping(value = "/getInstitutionInfoByName")
    public BaseResponse getInstitutionInfoByName(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        return ResultUtil.success(institutionService.getInstitutionInfoByName(institutionDTO));
    }


    @ApiOperation(value = "代理商下拉框信息一览")
    @PostMapping(value = "/getAgencyList")
    public BaseResponse getAgencyList(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        return ResultUtil.success(institutionService.getAgencyList(institutionDTO));
    }

    @ApiOperation(value = "代理商商户信息查询")
    @PostMapping(value = "/getAgencyInstitution")
    public BaseResponse getAgencyInstitution(@RequestBody @ApiParam @Valid QueryAgencyInstitutionDTO queryAgencyInstitutionDTO) {
        return ResultUtil.success(institutionService.getAgencyInstitution(queryAgencyInstitutionDTO));
    }

    @ApiOperation(value = "代理商商户信息导出")
    @PostMapping(value = "/exportAgencyInstitution")
    public List<AgencyInstitutionVO> exportAgencyInstitution(@RequestBody @ApiParam @Valid ExportAgencyInstitutionDTO exportAgencyInstitutionDTO) {
        return institutionService.exportAgencyInstitution(exportAgencyInstitutionDTO);
    }
}
