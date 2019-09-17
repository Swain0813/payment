package com.payment.clearing.service;

import com.payment.common.entity.Account;

import java.util.List;

public interface SettleOrdersService {

    /**
     * 定时跑批自动提款功能
     */
    void  getSettleOrders(String institutionCode, String currency, List<Account> lists);
}
