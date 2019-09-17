package com.payment.finance.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.dto.TradeCheckAccountDTO;
import com.payment.common.dto.TradeCheckAccountExportDTO;
import com.payment.common.entity.TradeCheckAccount;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TradeCheckAccountMapper extends BaseMapper<TradeCheckAccount> {

    /**
     * 分页查询交易对账信息
     *
     * @param tradeCheckAccountDTO 交易对账输入实体
     * @return 交易对账实体集合
     */
    List<TradeCheckAccountDTO> pageTradeCheckAccount(TradeCheckAccountDTO tradeCheckAccountDTO);

    /**
     * 导出交易对账总表信息
     *
     * @param tradeCheckAccountDTO
     * @return
     */
    List<TradeCheckAccount> exportTradeCheckAccount(TradeCheckAccountExportDTO tradeCheckAccountDTO);

}
