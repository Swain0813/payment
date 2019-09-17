package com.payment.permission.feign.trade.impl;

import com.payment.common.dto.*;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.vo.DccReportVO;
import com.payment.common.vo.QueryAgencyShareBenefitVO;
import com.payment.common.vo.QueryAgencyTradeVO;
import com.payment.common.vo.ShareBenefitReportVO;
import com.payment.permission.feign.trade.OrdersFeign;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.util.List;

/**
 * @description:
 * @author: XuWenQi
 * @create: 2019-03-05 16:20
 **/
@Component
public class OrdersFeignImpl implements OrdersFeign {

    @Override
    public BaseResponse exportInformation(OrdersDTO ordersDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getByMultipleConditions(OrdersDTO ordersDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getTradeDetail(String id,String type) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse calcOrdersAmount(@Valid CalcOrdersAmountDTO calcOrdersAmountDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse posGetOrders(PosSearchDTO posSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse posGetOrdersDetail(PosSearchDTO posSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getAllOrdersInfo(OrdersAllDTO ordersAllDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse exportAllOrders(OrdersExportAllDTO ordersAllDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getRelevantInfo(String institutionCode) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 机构交易一览导出
     * @param ordersAllDTO
     * @return
     */
    @Override
    public BaseResponse exportInstitutionOrders(OrdersExportAllDTO ordersAllDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getDccReport(DccReportDTO dccReportDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public List<DccReportVO> exportDccReport(DccReportExportDTO dccReportExportDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getAgencyTrade(QueryAgencyTradeDTO queryAgencyTradeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public List<QueryAgencyTradeVO> exportAgencyTrade(ExportAgencyTradeDTO exportAgencyTradeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getAgencyShareBenefit(QueryAgencyShareBenefitDTO queryAgencyShareBenefitDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public List<QueryAgencyShareBenefitVO> exportAgencyShareBenefit(ExportAgencyShareBenefitDTO exportAgencyShareBenefitDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getShareBenefitReport(QueryShareBenefitReportDTO queryShareBenefitReportDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public List<ShareBenefitReportVO> exportShareBenefitReport(ExportShareBenefitReportDTO exportShareBenefitReportDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
