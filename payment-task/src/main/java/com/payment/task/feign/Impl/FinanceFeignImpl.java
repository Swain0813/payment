package com.payment.task.feign.Impl;
import com.payment.common.dto.ReconOperDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.task.feign.FinanceFeign;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-04-16 16:13
 **/
@Component
public class FinanceFeignImpl implements FinanceFeign {

    @Override
    public BaseResponse selectTcsStFlow(String time) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse tradeAccountCheck() {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 结算跑批调调账操作
     * @param reconOperDTO
     * @return
     */
    @Override
    public BaseResponse doReconciliationBatch(ReconOperDTO reconOperDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 结算跑批调调账审核
     * @param reconciliationId
     * @param enabled
     * @param remark
     * @return
     */
    @Override
    public BaseResponse auditReconciliationBatch(String  reconciliationId, boolean enabled,String  remark) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
