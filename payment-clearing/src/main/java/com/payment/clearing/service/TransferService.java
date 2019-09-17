package com.payment.clearing.service;

import com.payment.common.dto.TransferFundDTO;
import com.payment.common.entity.TcsStFlow;
import com.payment.common.response.BaseResponse;

public interface TransferService {


    /**
     * 转账申请输入参数校验
     * @param cstar
     * @param md5key
     * @return
     */
    BaseResponse verificationAPIInputParamter(TransferFundDTO cstar, String md5key);

    /**
     * 转账功能
     * @param cstar
     * @param outst
     * @param inst
     * @return
     */
    BaseResponse stTransferAccount(TransferFundDTO cstar,TcsStFlow outst, TcsStFlow inst);
}
