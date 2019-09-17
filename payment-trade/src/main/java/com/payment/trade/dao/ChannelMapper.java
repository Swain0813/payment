package com.payment.trade.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Channel;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelMapper extends BaseMapper<Channel> {


    /**
     * 根据通道code查询Channel信息
     *
     * @param channelCode 通道编码
     * @return Channel实体
     */
    Channel selectByChannelCode(String channelCode);

    /**
     * 依据id查找通道
     *
     * @param channelId
     * @return
     */
    Channel selectById(@Param("channelId") String channelId);
}
