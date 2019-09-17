package com.payment.trade.controller;
import com.payment.common.base.BaseController;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.trade.dto.*;
import com.payment.trade.service.OnlineGatewayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@Api(description = "线上网关接口")
@RequestMapping("/onlineacquire")
public class OnlineAcquireController extends BaseController {

    @Autowired
    private OnlineGatewayService onlineGatewayService;

    @ApiOperation(value = "商户请求收单")
    @PostMapping("/gateway")
    @CrossOrigin
    public BaseResponse gateway(@RequestBody @ApiParam @Valid PlaceOrdersDTO placeOrdersDTO) {
        BaseResponse baseResponse = onlineGatewayService.gateway(placeOrdersDTO);
        if (StringUtils.isEmpty(baseResponse.getCode())) {
            baseResponse.setCode(EResultEnum.SUCCESS.getCode());
            baseResponse.setMsg("SUCCESS");
            return baseResponse;
        } else {
            //code不为空 msg不为空 返回通道的错误信息
            if (!StringUtils.isEmpty(baseResponse.getMsg())) {
                return baseResponse;
            }
            return ResultUtil.error(baseResponse.getCode(), this.getErrorMsgMap(baseResponse.getCode()));
        }
    }

    @ApiOperation(value = "模拟商户请求收单")
    @PostMapping("/imitateGateway")
    @CrossOrigin
    public BaseResponse imitateGateway(@RequestBody @ApiParam @Valid PlaceOrdersDTO placeOrdersDTO) {
        BaseResponse baseResponse = onlineGatewayService.imitateGateway(placeOrdersDTO);
        if (StringUtils.isEmpty(baseResponse.getCode())) {
            baseResponse.setCode(EResultEnum.SUCCESS.getCode());
            baseResponse.setMsg("SUCCESS");
            return baseResponse;
        } else {
            //code不为空 msg不为空 返回通道的错误信息
            if (!StringUtils.isEmpty(baseResponse.getMsg())) {
                return baseResponse;
            }
            return ResultUtil.error(baseResponse.getCode(), this.getErrorMsgMap(baseResponse.getCode()));
        }
    }

    @ApiOperation(value = "收银台所需的基础信息")
    @GetMapping("/cashier")
    @CrossOrigin
    public BaseResponse cashier(@RequestParam("orderId") @ApiParam String orderId) {
        return ResultUtil.success(onlineGatewayService.cashier(orderId, this.getLanguage()));
    }

    @ApiOperation(value = "收银台收单接口")
    @PostMapping("/cashierGateway")
    @CrossOrigin
    public BaseResponse cashierGateway(@RequestBody @ApiParam @Valid CashierDTO cashierDTO) {
        BaseResponse baseResponse = onlineGatewayService.cashierGateway(cashierDTO);
        if (StringUtils.isEmpty(baseResponse.getCode())) {
            baseResponse.setCode(EResultEnum.SUCCESS.getCode());
            baseResponse.setMsg("SUCCESS");
            return baseResponse;
        } else {
            return ResultUtil.error(baseResponse.getCode(), this.getErrorMsgMap(baseResponse.getCode()));
        }
    }


    @ApiOperation(value = "线上通道订单状态查询")
    @PostMapping("/onlineqOrderQuerying")
    @CrossOrigin
    public BaseResponse onlineqOrderQuerying(@RequestBody @ApiParam @Valid OnlineOrderQueryDTO OnlineOrderQueryDTO) {
        BaseResponse response = onlineGatewayService.onlineOrderQuery(OnlineOrderQueryDTO);
        if (StringUtils.isEmpty(response.getCode())) {
            response.setCode(EResultEnum.SUCCESS.getCode());
            response.setMsg("SUCCESS");
            return response;
        }
        return ResultUtil.error(response.getCode(), this.getErrorMsgMap(response.getCode()));
    }


    @ApiOperation(value = "线上通道订单状态查询RSA方式")
    @PostMapping("/onlineQuerying")
    public BaseResponse onlineqOrderQueryingUseRSA(@RequestBody @ApiParam @Valid OnlineOrderQueryRSADTO onlineOrderQueryRSADTO) {
        BaseResponse response = onlineGatewayService.onlineqOrderQueryingUseRSA(onlineOrderQueryRSADTO);
        if (StringUtils.isEmpty(response.getCode())) {
            response.setCode(EResultEnum.SUCCESS.getCode());
            response.setMsg("SUCCESS");
            return response;
        }
        return ResultUtil.error(response.getCode(), this.getErrorMsgMap(response.getCode()));
    }

    @ApiOperation(value = "线上订单查询")
    @PostMapping("/onlineOrderInfo")
    @CrossOrigin
    public BaseResponse onlineqOrderInfo(@RequestBody @ApiParam @Valid OnlineqOrderInfoDTO onlineqOrderInfoDTO) {
        onlineqOrderInfoDTO.setLanguage(this.getLanguage());
        return ResultUtil.success(onlineGatewayService.pageOnlineqOrderInfo(onlineqOrderInfoDTO));
    }


}
