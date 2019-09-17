package com.payment.permission.controller;

import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.ChargesTypeDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.ChargesFeign;
import com.payment.permission.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shenxinran
 * @Date: 2019/1/28 09:41
 * @Description: 算费管理
 */
@Api(description = "费率管理接口")
@RestController
@RequestMapping("/chargestype")
public class ChargesFeignController extends BaseController {

    @Autowired
    private ChargesFeign chargesFeign;

    @Autowired
    private OperationLogService operationLogService;

    @ApiOperation(value = "分页查询费率")
    @PostMapping("/pageChargesCondition")
    public BaseResponse pageChargesCondition(@RequestBody @ApiParam ChargesTypeDTO chargesTypeDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(chargesTypeDTO),
                "分页查询费率"));
        return chargesFeign.pageChargesCondition(chargesTypeDTO);
    }

    @ApiOperation(value = "根据ID查询费率")
    @PostMapping("/getChargesInfo")
    public BaseResponse getChargesInfo(@RequestBody ChargesTypeDTO chargesTypeDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(chargesTypeDTO),
                "根据ID查询费率"));
        return chargesFeign.getChargesInfo(chargesTypeDTO);
    }


    @ApiOperation(value = "新增费率")
    @PostMapping("/addChargesType")
    public BaseResponse addChargesType(@RequestBody @ApiParam ChargesTypeDTO chargesTypeDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(chargesTypeDTO),
                "新增费率"));
        return chargesFeign.addChargesType(chargesTypeDTO);
    }

    @ApiOperation(value = "修改费率")
    @PostMapping("/updateChargesType")
    public BaseResponse updateChargesType(@RequestBody @ApiParam ChargesTypeDTO chargesTypeDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(chargesTypeDTO),
                "修改费率"));
        return chargesFeign.updateChargesType(chargesTypeDTO);
    }

    @ApiOperation(value = "启用禁用费率")
    @PostMapping("/banChargesType")
    public BaseResponse banChargesType(@RequestBody @ApiParam ChargesTypeDTO chargesTypeDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(chargesTypeDTO),
                "启用禁用费率"));
        return chargesFeign.banChargesType(chargesTypeDTO);
    }
}
