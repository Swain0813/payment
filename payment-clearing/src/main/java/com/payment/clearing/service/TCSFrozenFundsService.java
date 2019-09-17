package com.payment.clearing.service;

import com.payment.common.entity.TcsFrozenFundsLogs;
import com.payment.common.response.BaseResponse;

public interface TCSFrozenFundsService {

    BaseResponse frozenFundsLogs(TcsFrozenFundsLogs ffl);
}
