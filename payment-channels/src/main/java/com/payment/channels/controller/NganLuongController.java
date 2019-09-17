package com.payment.channels.controller;

import com.payment.channels.service.NganLuongService;
import com.payment.common.base.BaseController;
import com.payment.common.dto.nganluong.NganLuongDTO;
import com.payment.common.response.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @description: NganLuong
 * @author: YangXu
 * @create: 2019-06-18 11:12
 **/
@RestController
@Api(description = "NganLuong")
@RequestMapping("/nganLuong")
public class NganLuongController extends BaseController {

    @Autowired
    private NganLuongService nganLuongService;

    @ApiOperation(value = "HELP2PAY收单接口")
    @PostMapping("/nganLuongPay")
    public BaseResponse nganLuongPay(@RequestBody @ApiParam @Valid NganLuongDTO nganLuongDTO) {
        return nganLuongService.nganLuongPay(nganLuongDTO);
    }

}
