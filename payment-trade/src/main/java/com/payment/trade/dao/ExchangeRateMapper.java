package com.payment.trade.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.ExchangeRate;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @Author XuWenQi
 * @Date 2019/2/20 15:20
 * @Descripate 汇率Mapper接口
 */
@Repository
public interface ExchangeRateMapper extends BaseMapper<ExchangeRate> {

    /**
     * 根据订单币种查询汇率
     *
     * @param orderCurrency 订单币种
     * @param tradeCurrency 交易币种
     * @return 汇率值
     */
    ExchangeRate selectRateByOrderCurrencyAndTradeCurrency(@Param("orderCurrency") String orderCurrency, @Param("tradeCurrency") String tradeCurrency);
}
