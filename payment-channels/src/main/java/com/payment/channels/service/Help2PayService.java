package com.payment.channels.service;

import com.payment.common.dto.help2pay.Help2PayOutDTO;
import com.payment.common.dto.help2pay.Help2PayRequestDTO;
import com.payment.common.response.BaseResponse;

public interface Help2PayService {

    /**
     * @Author YangXu
     * @Date 2019/5/28
     * @Descripate help2Pay收单接口
     * @return
     **/
    BaseResponse help2Pay(Help2PayRequestDTO help2PayRequestDTO);

    /**
     * @Author YangXu
     * @Date 2019/7/17
     * @Descripate HELP2PAY汇款接口
     * @return
     **/
    BaseResponse help2PayOut(Help2PayOutDTO help2PayOutDTO);
}
