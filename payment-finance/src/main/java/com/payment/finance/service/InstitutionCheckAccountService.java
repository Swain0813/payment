package com.payment.finance.service;

import com.payment.common.dto.TradeCheckAccountDTO;
import com.payment.common.dto.TradeCheckAccountExportDTO;
import com.payment.common.dto.TradeCheckAccountSettleExportDTO;
import com.payment.common.entity.SettleCheckAccount;
import com.payment.common.entity.SettleCheckAccountDetail;
import com.payment.common.vo.ExportTradeAccountVO;
import com.github.pagehelper.PageInfo;

import java.util.Date;
import java.util.Map;

public interface InstitutionCheckAccountService {

    /**
     * 机构交易信息对账
     *
     * @return
     */
    Object tradeAccountCheck();

    /**
     * 机构结算信息对账
     *
     * @return
     */
    int settleAccountCheck(Date time);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/16
     * @Descripate 分页查询机构结算对账
     **/
    PageInfo<SettleCheckAccount> pageSettleAccountCheck(TradeCheckAccountDTO tradeCheckAccountDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/16
     * @Descripate 分页查询机构结算对账详情
     **/
    PageInfo<SettleCheckAccountDetail> pageSettleAccountCheckDetail(TradeCheckAccountDTO tradeCheckAccountDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/16
     * @Descripate 导出机构结算对账
     **/
    Map<String, Object> exportSettleAccountCheck(TradeCheckAccountSettleExportDTO tradeCheckAccountDTO);


    /**
     * 分页查询交易对账总表信息
     *
     * @param tradeCheckAccountDTO 交易对账输入实体
     * @return 交易对账实体集合
     */
    PageInfo<TradeCheckAccountDTO> pageTradeCheckAccount(TradeCheckAccountDTO tradeCheckAccountDTO);

    /**
     * 导出交易对账总表信息
     *
     * @param tradeCheckAccountDTO
     * @return
     */
    ExportTradeAccountVO exportTradeCheckAccount(TradeCheckAccountExportDTO tradeCheckAccountDTO);
}
