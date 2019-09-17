package com.payment.channels.service;


import com.payment.common.dto.megapay.*;
import com.payment.common.response.BaseResponse;

public interface MegaPayService {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/5/28
     * @Descripate megaPay—THB收单接口
     **/
    BaseResponse megaPayTHB(MegaPayRequestDTO megaPayRequestDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/5/28
     * @Descripate megaPay—IDR收单接口
     **/
    BaseResponse megaPayIDR(MegaPayIDRRequestDTO megaPayIDRRequestDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/5/28
     * @Descripate nextPos收单接口
     **/
    BaseResponse nextPos(NextPosRequestDTO nextPosRequestDTO) throws Exception;

    /**
     * NextPos查询接口
     *
     * @param nextPosQueryDTO nextPos查询实体
     * @return BaseResponse
     */
    BaseResponse nextPosQuery(NextPosQueryDTO nextPosQueryDTO);

    /**
     * NextPos退款接口
     *
     * @param nextPosRefundDTO nextPos退款实体
     * @return BaseResponse
     */
    BaseResponse nextPosRefund(NextPosRefundDTO nextPosRefundDTO);
}
