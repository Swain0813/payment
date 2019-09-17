package com.payment.finance.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Channel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ChannelMapper extends BaseMapper<Channel> {


    /**
     * @Author YangXu
     * @Date 2019/4/30
     * @Descripate  查询通道编码
     * @return
     **/
    @Select("select channel_code from channel where channel_en_name like CONCAT(CONCAT('%', #{name}), '%') ")
    List<String> getChannelCodeByname(@Param("name") String name);

}
