package com.payment.institution.service;

import com.payment.common.base.BaseService;
import com.payment.common.dto.PaymentModeDTO;
import com.payment.common.vo.PaymentModeVO;
import com.payment.institution.entity.PaymentMode;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 支付方式管理业务接口
 */
public interface PaymentModeService extends BaseService<PaymentMode> {

    /**
     * 添加支付方式
     *
     * @param paymentModeDTO
     * @return 1 or 0
     */
    int addPayinfo(PaymentModeDTO paymentModeDTO);


    /**
     * 查询支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    PageInfo<PaymentModeVO> pagePayInfo(PaymentModeDTO paymentModeDTO);

    /**
     * 查询所有支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    List<PaymentModeVO> getPayInfo(PaymentModeDTO paymentModeDTO);

    /**
     * 启用禁用支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    int banPayInfo(PaymentModeDTO paymentModeDTO);

    /**
     * 添加不同语言的支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    int addOtherLanguage(PaymentModeDTO paymentModeDTO);

    /**
     * 更新支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    int updatePayInfo(PaymentModeDTO paymentModeDTO);
}
