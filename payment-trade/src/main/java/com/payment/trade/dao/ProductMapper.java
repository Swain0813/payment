package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * @param payType        支付方式
     * @param tradeCurrency  订单币种
     * @param tradeDirection 交易方向
     * @return 产品
     */
    Product selectByPayTypeAndCurrencyAndTradeDirection(@Param("payType") String payType, @Param("tradeCurrency") String tradeCurrency, @Param("tradeDirection") Byte tradeDirection);

    /**
     * @param productCode 产品code
     * @return 产品
     */
    Product selectByProductCode(Integer productCode);

    /**
     * @Author YangXu
     * @Date 2019/7/23
     * @Descripate 根据机构ID和订单币种查询付款产品
     * 付款对应的产品的交易类型是付
     * @return
     **/
    Product selectByCurrencyAndInstitutionId(@Param("orderCurrency") String orderCurrency, @Param("institutionId") String institutionId);

  /**
   * @Author YangXu
   * @Date 2019/8/23
   * @Descripate
   * @return 根据机构ID和订单币种和产品类型查询产品
   **/
    Product selectByCurrencyAndCodeAndType(@Param("productCode") Integer productCode, @Param("institutionId") String institutionId,@Param("type") Byte type);
}
