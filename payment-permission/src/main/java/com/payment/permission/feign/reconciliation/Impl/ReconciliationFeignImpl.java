package com.payment.permission.feign.reconciliation.Impl;

import com.payment.common.dto.*;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.reconciliation.ReconciliationFeign;
import org.springframework.stereotype.Component;

import javax.validation.Valid;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-03-29 14:27
 **/
@Component
public class ReconciliationFeignImpl implements ReconciliationFeign {

    @Override
    public BaseResponse pageReconciliation(ReconciliationDTO reconciliationDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageReviewReconciliation(ReconciliationDTO reconciliationDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse exportReconciliation(ReconciliationExportDTO reconciliationDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse updateCheckAccount(String checkAccountId, String remark) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }


    @Override
    public BaseResponse auditCheckAccount(String checkAccountId, Boolean enable, String remark) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse doReconciliation(ReconOperDTO reconOperDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse auditReconciliation(String reconciliationId, boolean enabled, String remark) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageAccountCheckLog(SearchAccountCheckDTO searchAccountCheckDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageAccountCheck(SearchAccountCheckDTO searchAccountCheckDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageAccountCheckAudit(SearchAccountCheckDTO searchAccountCheckDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageSettleAccountCheck(TradeCheckAccountDTO tradeCheckAccountDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageSettleAccountCheckDetail(TradeCheckAccountDTO tradeCheckAccountDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse exportAccountCheck(SearchAccountCheckDTO searchAccountCheckDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse exportAccountCheckAudit(SearchAccountCheckDTO searchAccountCheckDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse exportSettleAccountCheck(TradeCheckAccountSettleExportDTO tradeCheckAccountDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getAvailableBalance(@Valid SearchAvaBalDTO searchAvaBalDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
