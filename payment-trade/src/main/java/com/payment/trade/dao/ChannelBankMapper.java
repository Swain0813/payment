package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.ChannelBank;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelBankMapper extends BaseMapper<ChannelBank> {


    /**
     * 查找ChannelBank
     *
     * @param id
     * @return
     */
    ChannelBank selectById(@Param("id") String id);
}
