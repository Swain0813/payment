package com.payment.clearing.service;

public interface DrawService {
    /**
     * 按商户分组自动提款批处理
     */
    void DrawForBatch();
}
