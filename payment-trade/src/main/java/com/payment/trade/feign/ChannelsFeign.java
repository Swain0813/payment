package com.payment.trade.feign;

import com.payment.common.dto.alipay.*;
import com.payment.common.dto.eghl.EGHLRequestDTO;
import com.payment.common.dto.enets.EnetsBankRequestDTO;
import com.payment.common.dto.enets.EnetsOffLineRequestDTO;
import com.payment.common.dto.help2pay.Help2PayOutDTO;
import com.payment.common.dto.help2pay.Help2PayRequestDTO;
import com.payment.common.dto.megapay.*;
import com.payment.common.dto.nganluong.NganLuongDTO;
import com.payment.common.dto.vtc.VTCRequestDTO;
import com.payment.common.dto.wechat.*;
import com.payment.common.dto.xendit.XenditDTO;
import com.payment.common.response.BaseResponse;
import com.payment.trade.feign.Impl.ChannelsFeignImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * 通道Feign
 */
@FeignClient(value = "payment-channels", fallback = ChannelsFeignImpl.class)
public interface ChannelsFeign {

    @ApiOperation(value = "支付宝线下BSC接口")
    @PostMapping("/aliPay/aliPayOfflineBSC")
    BaseResponse aliPayOfflineBSC(@RequestBody @ApiParam @Valid AliPayOfflineBSCDTO aliPayOfflineBSCDTO);

    @ApiOperation(value = "支付宝CSB接口")
    @PostMapping("/aliPay/aliPayCSB")
    BaseResponse aliPayCSB(@RequestBody @ApiParam @Valid AliPayCSBDTO aliPayCSBDTO);

    @ApiOperation(value = "支付宝退款接口")
    @PostMapping("/aliPay/alipayRefund")
    BaseResponse alipayRefund(@RequestBody @ApiParam @Valid AliPayRefundDTO aliPayRefundDTO);

    @ApiOperation(value = "支付宝查询接口")
    @PostMapping("/aliPay/alipayQuery")
    BaseResponse alipayQuery(@RequestBody @ApiParam @Valid AliPayQueryDTO aliPayQueryDTO);

    @ApiOperation(value = "支付宝撤销接口")
    @PostMapping("/aliPay/alipayCancel")
    BaseResponse alipayCancel(@RequestBody @ApiParam @Valid AliPayCancelDTO aliPayCancelDTO);

    @ApiOperation("微信查询接口")
    @PostMapping("/wechat/wechatQuery")
    BaseResponse wechatQuery(@RequestBody @ApiParam @Valid WechatQueryDTO wechatQueryDTO);

    @ApiOperation("微信线下BSC接口")
    @PostMapping("/wechat/wechatBSC")
    BaseResponse wechatOfflineBSC(@RequestBody @ApiParam @Valid WechatBSCDTO wechatBSCDTO);

    @ApiOperation("微信线下CSB接口")
    @PostMapping("/wechat/wechatCSB")
    BaseResponse wechatOfflineCSB(@RequestBody @ApiParam @Valid WechatCSBDTO wechatCSBDTO);

    @ApiOperation("微信退款接口")
    @PostMapping("/wechat/wechatRefund")
    BaseResponse wechatRefund(@RequestBody @ApiParam @Valid WechaRefundDTO wechaRefundDTO);

    @ApiOperation(value = "微信撤销接口")
    @PostMapping("/wechat/wechatCancel")
    BaseResponse wechatCancel(@RequestBody @ApiParam @Valid WechatCancelDTO wechatCancelDTO);

    @ApiOperation(value = "eghl收单接口")
    @PostMapping("/eghl/eGHLPay")
    BaseResponse eGHLPay(@RequestBody @ApiParam @Valid EGHLRequestDTO eghlRequestDTO);

    @ApiOperation(value = "megaPayTHB网银收单接口")
    @PostMapping("/megaPay/megaPayTHB")
    BaseResponse megaPayTHB(@RequestBody @ApiParam @Valid MegaPayRequestDTO megaPayRequestDTO);

    @ApiOperation(value = "megaPayIDR网银收单接口")
    @PostMapping("/megaPay/megaPayIDR")
    BaseResponse megaPayIDR(@RequestBody @ApiParam @Valid MegaPayIDRRequestDTO megaPayIDRRequestDTO);

    @ApiOperation(value = "nextPos扫码接口")
    @PostMapping("/megaPay/nextPos")
    BaseResponse nextPos(@RequestBody @ApiParam @Valid NextPosRequestDTO nextPosRequestDTO);

    @ApiOperation(value = "nextPos查询接口")
    @PostMapping("/megaPay/nextPosQuery")
    BaseResponse nextPosQuery(@RequestBody @ApiParam NextPosQueryDTO nextPosQueryDTO);

    @ApiOperation(value = "nextPos退款接口")
    @PostMapping("/megaPay/nextPosRefund")
    BaseResponse nextPosRefund(@RequestBody @ApiParam NextPosRefundDTO nextPosRefundDTO);

    @ApiOperation(value = "vtc收单接口")
    @PostMapping("/vtc/vtcPay")
    BaseResponse vtcPay(@RequestBody @ApiParam @Valid VTCRequestDTO vtcRequestDTO);

    @ApiOperation(value = "enets网银收单接口")
    @PostMapping("enets/eNetsDebitPay")
    BaseResponse eNetsBankPay(@RequestBody @ApiParam @Valid EnetsBankRequestDTO enetsBankRequestDTO);

    @ApiOperation(value = "enetsPos线下CSB")
    @PostMapping("enets/NPSQRCodePay")
    BaseResponse eNetsPosCSBPay(@RequestBody @ApiParam @Valid EnetsOffLineRequestDTO enetsOffLineRequestDTO);

    @ApiOperation(value = "help2Pay网银收单接口")
    @PostMapping("help/help2pay")
    BaseResponse help2Pay(@RequestBody @ApiParam @Valid Help2PayRequestDTO help2PayRequestDTO);

    @ApiOperation(value = "help2Pay网银汇款接口")
    @PostMapping("help/help2PayOut")
    BaseResponse help2PayOut(@RequestBody @ApiParam @Valid Help2PayOutDTO help2PayOutDTO);

    @ApiOperation(value = "nganLuong网银收单接口")
    @PostMapping("nganLuong/nganLuongPay")
    BaseResponse nganLuongPay(@RequestBody @ApiParam @Valid NganLuongDTO nganLuongDTO);

    @ApiOperation(value = "xendit网银收单接口")
    @PostMapping("xendit/xenditPay")
    BaseResponse xenditPay(@RequestBody @ApiParam @Valid XenditDTO xenditDTO);
}
