package com.payment.finance.service;

import com.payment.common.dto.ReviewSettleDTO;
import com.payment.common.dto.SettleOrderDTO;
import com.payment.common.dto.SettleOrderExportDTO;
import com.payment.common.dto.WithdrawalDTO;
import com.payment.common.entity.SettleOrder;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 机构结算交易
 */
public interface SettleOrderService {

    /**
     * 机构结算交易一览查询
     *
     * @param settleOrderDTO
     * @return
     */
    PageInfo<SettleOrder> pageSettleOrder(SettleOrderDTO settleOrderDTO);

    /**
     * 机构结算交易详情
     *
     * @param settleOrderDTO
     * @return
     */
    PageInfo<SettleOrder> pageSettleOrderDetail(SettleOrderExportDTO settleOrderDTO);

    /**
     * * 机构结算导出
     *
     * @param settleOrderDTO
     * @return
     */
    List<SettleOrder> exportSettleOrder(SettleOrderDTO settleOrderDTO);

    /**
     * 机构审核
     *
     * @param reviewSettleDTO
     * @return
     */
    int reviewSettlement(ReviewSettleDTO reviewSettleDTO);

    /**
     * 校验密码
     *
     * @param oldPassword
     * @param password
     * @return
     */
    Boolean checkPassword(String oldPassword, String password);

    /**
     * 机构结算查询一览详情
     * @param settleOrderDTO
     * @return
     */
    PageInfo<SettleOrder> pageSettleOrderQuery(SettleOrderExportDTO settleOrderDTO);

    /**
     * 手动提款
     * @param withdrawalDTO
     * @param userName
     */
    void withdrawal(WithdrawalDTO withdrawalDTO,String userName);
}
