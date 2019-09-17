package com.payment.channels.controller;

import com.payment.channels.service.EghlService;
import com.payment.common.base.BaseController;
import com.payment.common.dto.eghl.EGHLRequestDTO;
import com.payment.common.response.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @description: EGHL
 * @author: YangXu
 * @create: 2019-05-28 14:14
 **/
@RestController
@Api(description = "EGHL")
@RequestMapping("/eghl")
public class EghlController extends BaseController {

    @Autowired
    private EghlService eghlService;

    @ApiOperation(value = "eghl收单接口")
    @PostMapping("eGHLPay")
    public BaseResponse eGHLPay(@RequestBody @ApiParam @Valid EGHLRequestDTO eghlRequestDTO) {
        return eghlService.eGHLPay(eghlRequestDTO);
    }
}
