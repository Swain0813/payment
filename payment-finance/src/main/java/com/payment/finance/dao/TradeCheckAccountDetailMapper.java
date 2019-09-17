package com.payment.finance.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.TradeCheckAccountExportDTO;
import com.payment.common.entity.TradeCheckAccountDetail;
import com.payment.common.vo.TradeAccountDetailVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeCheckAccountDetailMapper extends BaseMapper<TradeCheckAccountDetail> {

    /**
     * 导出交易对账详细表信息
     *
     * @param tradeCheckAccountDTO
     * @return
     */
    List<TradeAccountDetailVO> exportTradeCheckAccount(TradeCheckAccountExportDTO tradeCheckAccountDTO);
}
