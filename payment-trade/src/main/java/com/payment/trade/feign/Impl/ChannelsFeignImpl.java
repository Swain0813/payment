package com.payment.trade.feign.Impl;

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
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.trade.feign.ChannelsFeign;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
public class ChannelsFeignImpl implements ChannelsFeign {

    @Override
    public BaseResponse eGHLPay(EGHLRequestDTO eghlRequestDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse megaPayTHB(MegaPayRequestDTO megaPayRequestDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse megaPayIDR(MegaPayIDRRequestDTO megaPayIDRRequestDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse vtcPay(VTCRequestDTO vtcRequestDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse eNetsBankPay(EnetsBankRequestDTO enetsBankRequestDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse help2Pay(Help2PayRequestDTO help2PayRequestDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse help2PayOut(Help2PayOutDTO help2PayOutDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse nextPos(@Valid NextPosRequestDTO nextPosRequestDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse eNetsPosCSBPay(EnetsOffLineRequestDTO enetsOffLineRequestDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse aliPayOfflineBSC(AliPayOfflineBSCDTO aliPayOfflineBSCDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse aliPayCSB(@Valid AliPayCSBDTO aliPayCSBDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse nganLuongPay(NganLuongDTO nganLuongDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse xenditPay(@Valid XenditDTO xenditDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse alipayRefund(@Valid AliPayRefundDTO aliPayRefundDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse wechatOfflineBSC(@Valid WechatBSCDTO wechatBSCDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse wechatOfflineCSB(@Valid WechatCSBDTO wechatCSBDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse wechatRefund(@Valid WechaRefundDTO wechaRefundDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse alipayQuery(@Valid AliPayQueryDTO aliPayQueryDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse wechatQuery(@Valid WechatQueryDTO wechatQueryDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse alipayCancel(@Valid AliPayCancelDTO aliPayCancelDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse wechatCancel(@Valid WechatCancelDTO wechatCancelDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse nextPosRefund(NextPosRefundDTO nextPosRefundDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse nextPosQuery(NextPosQueryDTO nextPosQueryDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
