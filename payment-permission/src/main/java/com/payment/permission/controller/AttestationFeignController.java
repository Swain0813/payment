package com.payment.permission.controller;

import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.AttestationDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.AttestationFeign;
import com.payment.permission.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author shenxinran
 * @Date: 2019/2/18 18:23
 * @Description: 密钥管理Feign Controller
 */
@RestController
@RequestMapping("/attestation")
@Api(description = "密钥管理")
public class AttestationFeignController extends BaseController {

    @Autowired
    private AttestationFeign attestationFeign;
    /**
     * 操作日志模块
     */
    @Autowired
    private OperationLogService operationLogService;

    @ApiOperation(value = "生成RSA公私钥")
    @GetMapping("/getRSA")
    public BaseResponse getRSA() {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, null,
                "生成RSA公私钥"));
        return attestationFeign.getRSA();
    }

    @ApiOperation(value = "添加密钥")
    @PostMapping("/addKey")
    public BaseResponse addKey(@RequestBody @ApiParam AttestationDTO attestationDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(attestationDTO),
                "添加密钥"));
        return attestationFeign.addKey(attestationDTO);
    }

    @ApiOperation(value = "查询商户公钥")
    @PostMapping("/pageKeyInfo")
    public BaseResponse pageKeyInfo(@RequestBody @ApiParam AttestationDTO attestationDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(attestationDTO),
                "查询商户公钥"));
        return attestationFeign.pageKeyInfo(attestationDTO);
    }

    @ApiOperation(value = "查询密钥所有信息")
    @PostMapping("/pageAllKeyInfo")
    public BaseResponse pageAllKeyInfo(@RequestBody @ApiParam AttestationDTO attestationDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(attestationDTO),
                "查询密钥所有信息"));
        return attestationFeign.pageAllKeyInfo(attestationDTO);
    }

    @ApiOperation(value = "修改密钥信息")
    @PostMapping("/updateKeyInfo")
    public BaseResponse updateKeyInfo(@RequestBody @ApiParam AttestationDTO attestationDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(attestationDTO),
                "修改密钥信息"));
        return attestationFeign.updateKeyInfo(attestationDTO);
    }

    @ApiOperation(value = "启用禁用")
    @PostMapping("/banKeyInfo")
    public BaseResponse banKeyInfo(@RequestBody @ApiParam AttestationDTO attestationDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(attestationDTO),
                "启用禁用"));
        return attestationFeign.banKeyInfo(attestationDTO);
    }


}
