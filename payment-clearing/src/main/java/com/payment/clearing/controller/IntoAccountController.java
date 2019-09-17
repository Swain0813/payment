package com.payment.clearing.controller;
import com.payment.clearing.service.IntoAccountService;
import com.payment.clearing.vo.IntoAndOutMerhtAccountRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description: 资金变动接口
 * @author: YangXu
 * @create: 2019-07-25 10:50
 **/
@RestController
@Api(description = "资金变动接口")
@RequestMapping("/IntoAccountAction")
public class IntoAccountController {

    @Autowired
    private IntoAccountService intoAccountService;


    @ApiOperation(value = "资金变动接口")
    @PostMapping("v1/IntoAndOutMerhtAccount")
    public IntoAndOutMerhtAccountRequest intoAndOutMerhtAccount(@RequestBody @ApiParam IntoAndOutMerhtAccountRequest intoAndOutMerhtAccountRequest) {
        return intoAccountService.intoAndOutMerhtAccount(intoAndOutMerhtAccountRequest);
    }


}
