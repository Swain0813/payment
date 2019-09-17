package com.payment.channels.service;

import com.payment.common.dto.nganluong.NganLuongDTO;
import com.payment.common.response.BaseResponse;

public interface NganLuongService {


    /**
     * @Author YangXu
     * @Date 2019/5/28
     * @Descripate NganLuong收单接口
     * @return
     **/
    BaseResponse nganLuongPay( NganLuongDTO nganLuongDTO);
}
