package com.payment.trade.controller;
import com.payment.common.base.BaseController;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.trade.dto.PlaceOrdersDTO;
import com.payment.trade.dto.PosGetOrdersDTO;
import com.payment.trade.dto.TerminalQueryOrdersDTO;
import com.payment.trade.dto.TerminalQueryRelevantDTO;
import com.payment.trade.service.SameScanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @description: 线下交易接口
 * @author: XuWenQi
 * @create: 2019-02-12 15:20
 **/
@RestController
@Api(description = "线下同机构动态扫码")
@Slf4j
@RequestMapping("/trade")
public class SameScanController extends BaseController {

    @Autowired
    private SameScanService sameScanService;

    @ApiOperation(value = "线下同机构CSB动态扫码")
    @PostMapping("csbScan")
    public BaseResponse csbScan(@RequestBody @ApiParam @Valid PlaceOrdersDTO placeOrdersDTO) {
        BaseResponse baseResponse = sameScanService.csbScan(placeOrdersDTO);
        String code = baseResponse.getCode();//业务返回码
        if (StringUtils.isEmpty(code)) {
            baseResponse.setCode(EResultEnum.SUCCESS.getCode());
            baseResponse.setMsg("SUCCESS");
            return baseResponse;
        }
        //code不为空 msg不为空 返回通道的错误信息
        if (!StringUtils.isEmpty(baseResponse.getMsg())) {
            return baseResponse;
        }
        return ResultUtil.error(code, this.getErrorMsgMap(code));
    }

    @ApiOperation(value = "线下同机构BSC动态扫码")
    @PostMapping("bscScan")
    public BaseResponse bscScan(@RequestBody @ApiParam @Valid PlaceOrdersDTO placeOrdersDTO) {
        BaseResponse baseResponse = sameScanService.bscScan(placeOrdersDTO);
        String code = baseResponse.getCode();//业务返回码
        if (StringUtils.isEmpty(code)) {
            baseResponse.setCode(EResultEnum.SUCCESS.getCode());
            baseResponse.setMsg("SUCCESS");
            return baseResponse;
        }
        //code不为空 msg不为空 返回通道的错误信息
        if (!StringUtils.isEmpty(baseResponse.getMsg())) {
            return baseResponse;
        }
        return ResultUtil.error(code, this.getErrorMsgMap(code));
    }

    /**
     * 线下api--查询线下订单信息
     * @param posGetOrdersDTO
     * @return
     */
    @ApiOperation(value = "线下查询订单列表")
    @PostMapping("terminalQueryOrderList")
    public BaseResponse terminalQueryOrderList(@RequestBody @ApiParam @Valid PosGetOrdersDTO posGetOrdersDTO) {
        return ResultUtil.success(sameScanService.terminalQueryOrderList(posGetOrdersDTO));
    }

    @ApiOperation(value = "pos机查询订单状态接口")
    @PostMapping("terminalQueryOrderStatus")
    public BaseResponse terminalQueryOrder(@RequestBody @ApiParam @Valid TerminalQueryOrdersDTO terminalQueryDTO) {
        return ResultUtil.success(sameScanService.terminalQueryOrderStatus(terminalQueryDTO));
    }

    @ApiOperation(value = "pos机分页查询订单列表")
    @PostMapping("posQueryOrderList")
    public BaseResponse posQueryOrderList(@RequestBody @ApiParam @Valid PosGetOrdersDTO posGetOrdersDTO) {
        return ResultUtil.success(sameScanService.posQueryOrderList(posGetOrdersDTO));
    }

    @ApiOperation(value = "pos机查询订单详情")
    @PostMapping("terminalQueryOrderDetail")
    public BaseResponse terminalQueryOrderDetail(@RequestBody @ApiParam @Valid TerminalQueryOrdersDTO terminalQueryDTO) {
        return ResultUtil.success(sameScanService.terminalQueryOrderDetail(terminalQueryDTO));
    }

    @ApiOperation(value = "pos机查询机构关联信息接口")
    @PostMapping("terminalQueryRelevantInfo")
    public BaseResponse terminalQueryRelevantInfo(@RequestBody @ApiParam @Valid TerminalQueryRelevantDTO terminalQueryDTO) {
        return ResultUtil.success(sameScanService.terminalQueryRelevantInfo(terminalQueryDTO));
    }

    @ApiOperation(value = "机构分配通道查询关联关系")
    @GetMapping("getRelevantInfo")
    @CrossOrigin
    public BaseResponse getRelevantInfo(@RequestParam @ApiParam String institutionCode, HttpServletResponse response) {
        return ResultUtil.success(sameScanService.getRelevantInfo(institutionCode));
    }

    @ApiOperation(value = "收银台查询机构产品,产品通道信息")
    @GetMapping("getRelevantInfoSy")
    @CrossOrigin
    public BaseResponse getRelevantInfoSy(@RequestParam @ApiParam String institutionCode, HttpServletResponse response) {
        return ResultUtil.success(sameScanService.getRelevantInfoSy(institutionCode));
    }

}

