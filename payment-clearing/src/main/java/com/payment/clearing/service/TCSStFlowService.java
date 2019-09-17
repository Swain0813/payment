package com.payment.clearing.service;

import com.payment.clearing.vo.IntoAndOutMerhtAccountRequest;
import com.payment.common.entity.TcsStFlow;
import com.payment.common.response.BaseResponse;

import java.util.List;


public interface TCSStFlowService {


    /**
     * @return
     * @Author YangXu
     * @Date 2019/7/25
     * @Descripate 结算账户的资金变动处理方法，主要包含插入结算表记录
     **/
    BaseResponse IntoAndOutMerhtSTAccount2(IntoAndOutMerhtAccountRequest ioma);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/7/29
     * @Descripate 根据流程梳理要求优化的以组为单位结算并提交事物
     **/
    void SettlementForMerchantGroup2(String merchantid, String sltcurrency, List<TcsStFlow> list);

}
