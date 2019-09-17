package com.payment.permission.feign.finance.impl;

import com.payment.common.dto.ReviewSettleDTO;
import com.payment.common.dto.SettleOrderDTO;
import com.payment.common.dto.SettleOrderExportDTO;
import com.payment.common.dto.WithdrawalDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.finance.SettleOrderFeign;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 机构结算交易相关模块
 */
@Service
public class SettleOrderFeignImpl implements SettleOrderFeign {


    /**
     * 机构结算交易分页一览查询
     *
     * @param settleOrderDTO
     * @return
     */
    @Override
    public BaseResponse pageSettleOrder(@RequestBody @ApiParam SettleOrderDTO settleOrderDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 机构结算交易详情分页查询
     *
     * @param settleOrderDTO
     * @return
     */
    @Override
    public BaseResponse pageSettleOrderDetail(@RequestBody @ApiParam SettleOrderDTO settleOrderDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 机构结算审核
     *
     * @param reviewSettleDTO
     * @return
     */
    @Override
    public BaseResponse reviewSettlement(ReviewSettleDTO reviewSettleDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 机构结算查询一览详情
     *
     * @param settleOrderDTO
     * @return
     */
    @Override
    public BaseResponse pageSettleOrderQuery(SettleOrderExportDTO settleOrderDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 导出详情
     *
     * @param settleOrderDTO
     * @return
     */
    @Override
    public BaseResponse exportSettleOrder(SettleOrderDTO settleOrderDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 手动跑批
     *
     *
     * @param withdrawalDTO@return
     */
    @Override
    public BaseResponse withdrawal(WithdrawalDTO withdrawalDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
