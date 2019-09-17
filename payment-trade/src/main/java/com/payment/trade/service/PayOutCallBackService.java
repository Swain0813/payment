package com.payment.trade.service;
import com.payment.trade.dto.Help2PayOutCallbackDTO;


public interface PayOutCallBackService {


    /**
     * @Author YangXu
     * @Date 2019/8/5
     * @Descripate Help2Pay付款回调接口
     * @return
     **/
    void help2PayCallBack(Help2PayOutCallbackDTO help2PayOutCallbackDTO);

}
