package com.payment.task.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.ExchangeRate;
import com.payment.common.vo.ExchangeRateVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author XuWenQi
 * @Date 2019/1/25 14:20
 * @Descripate 汇率Mapper接口
 */
@Repository
public interface ExchangeRateMapper extends BaseMapper<ExchangeRate> {

    /**
     * 根据本币与外币禁用汇率
     *
     * @param modifier        修改者
     * @param localCurrency   本位币种
     * @param foreignCurrency 目标币种
     * @return 修改条数
     */
    int updateStatusByLocalCurrencyAndForeignCurrency(@Param("localCurrency") String localCurrency, @Param("foreignCurrency") String foreignCurrency, @Param("modifier") String modifier);

    /**
     * 根据创建时间查询汇率信息
     *
     * @param date 创建日期
     * @return 汇率实体集合
     */
    List<ExchangeRateVO> selectByCreateTimeAndCreator(@Param("date") String date, @Param("creator") String creator);

    /**
     * 删除禁用的汇率数据
     *
     * @return
     */
    @Delete("DELETE FROM exchange_rate WHERE enabled=false")
    int deleteExchangeRate();
}
