package com.payment.permission.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Channel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelMapper extends BaseMapper<Channel> {

    /**
     * 查询所有的启用的通道code
     * @return
     */
    List<String> selectAllChannelCode();

}
