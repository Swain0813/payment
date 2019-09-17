package com.payment.clearing.service;

import com.payment.clearing.vo.IntoAndOutMerhtAccountRequest;

public interface IntoAccountService {


    /**
     * @Author YangXu
     * @Date 2019/7/25
     * @Descripate 资金变动接口
     * @return
     **/
    IntoAndOutMerhtAccountRequest intoAndOutMerhtAccount(IntoAndOutMerhtAccountRequest intoAndOutMerhtAccountRequest);
}
