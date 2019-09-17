package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.*;
import com.payment.common.entity.Bank;
import com.payment.common.entity.BankIssuerid;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.institution.ChannelFeign;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-30 14:38
 **/
@Component
public class ChannelFeignImpl implements ChannelFeign {

    @Override
    public BaseResponse addChannel(ChannelDTO channelDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse updateChannel(ChannelDTO channelDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageFindChannel(ChannelDTO channelDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getAllChannel() {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageFindProductChannel(SearchChannelDTO searchChannelDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse banChannel(String channelId, Boolean enabled) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getChannelById(String channelId) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getChannelByProductId(String productId) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getChannelByInsIdAndProId(String institutionId, String productId) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse exportAllChannels(ChannelDTO channelDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse addBankIssureId( List<BankIssuerid> bankIssuerid) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse updateBankIssureId(BankIssuerid bankIssuerid) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageFindBankIssuerid(BankIssueridDTO bankIssuerid) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 导出银行issureid对照信息
     *
     * @param bankIssuerid
     * @return
     */
    @Override
    public BaseResponse exportBankIssuerid(BankIssueridExportDTO bankIssuerid) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse addBank(Bank bankIssuerid) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse updateBank(Bank bankIssuerid) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageFindBank(BankDTO bank) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
