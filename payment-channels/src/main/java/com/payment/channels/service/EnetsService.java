package com.payment.channels.service;
import com.payment.common.dto.enets.EnetsBankRequestDTO;
import com.payment.common.dto.enets.EnetsOffLineRequestDTO;
import com.payment.common.response.BaseResponse;

public interface EnetsService {

    /**
     * @Author YangXu
     * @Date 2019/5/28
     * @Descripate eNets网银收单接口
     * @return
     **/
    BaseResponse eNetsDebitPay( EnetsBankRequestDTO enetsBankRequestDTO);

    /**
     * @Author YangXu
     * @Date 2019/5/28
     * @Descripate eNets线下收单接口
     * @return
     **/
    BaseResponse NPSQRCodePay( EnetsOffLineRequestDTO enetsOffLineRequestDTO);

}
