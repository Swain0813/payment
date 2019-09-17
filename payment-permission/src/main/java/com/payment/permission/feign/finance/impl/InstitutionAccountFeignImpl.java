package com.payment.permission.feign.finance.impl;

import com.payment.common.dto.TradeCheckAccountDTO;
import com.payment.common.dto.TradeCheckAccountExportDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.vo.ExportTradeAccountVO;
import com.payment.permission.feign.finance.InstitutionAccountFeign;
import org.springframework.stereotype.Service;

@Service
public class InstitutionAccountFeignImpl implements InstitutionAccountFeign {

    /**
     * 分页查询交易对账总表信息
     *
     * @param tradeCheckAccountDTO
     * @return
     */
    @Override
    public BaseResponse pageTradeCheckAccount(TradeCheckAccountDTO tradeCheckAccountDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 导出交易对账信息
     *
     * @param tradeCheckAccountDTO
     * @return
     */
    @Override
    public ExportTradeAccountVO exportTradeCheckAccount(TradeCheckAccountExportDTO tradeCheckAccountDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
