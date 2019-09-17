package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Dictionary;
import com.payment.trade.vo.CurrencyVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DictionaryMapper extends BaseMapper<Dictionary> {

    /**
     * 根据code查询默认值
     *
     * @param code
     * @return
     */
    @Select("select default_value from dictionary where code = #{code} and enabled = true")
    String selectByCurrency(@Param("code") String code);

    /**
     * 查询币种
     *
     * @param currency
     * @return
     */
    @Select("select code from dictionary where code = #{currency} and enabled = true")
    String getCurrency(String currency);

    /**
     * 查询所有的AW启用的币种
     *
     * @param
     * @return
     */
    @Select("select code as currency,default_value as defaultValue from dictionary where dictype_code=#{dicTypeCode} and enabled=true order by create_time desc")
    List<CurrencyVO> selectDictionaryLists(@Param("dicTypeCode") String dicTypeCode);
}
