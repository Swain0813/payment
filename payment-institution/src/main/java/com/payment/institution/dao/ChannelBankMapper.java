package com.payment.institution.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.ChannelBank;
import com.payment.common.vo.ChaBankRelVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChannelBankMapper extends BaseMapper<ChannelBank> {

    /**
     * 根据通道id删除通道银行中间表的记录
      * @param channelId
     */
    @Delete("DELETE FROM channel_bank WHERE channel_id = #{channelId}")
    void deleteByChannelId(@Param("channelId") String channelId);

    ChaBankRelVO getInfoByCIdAndBId(@Param("channelId")String channelId,@Param("bankId") String bankId);


    List<ChannelBank> selectByChannelId(@Param("channelId") String channelId);
}
