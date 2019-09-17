package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.ChannelDTO;
import com.payment.common.dto.ChannelExportDTO;
import com.payment.common.entity.Channel;
import com.payment.common.vo.ChanProVO;
import com.payment.common.vo.ChannelExportVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelMapper extends BaseMapper<Channel> {


    /**
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 根据渠道名称查询是否存在
     * @return
     **/
    @Select("select count(1)  from channel where channel_cn_name = #{cnName} and currency = #{currency}")
    int getChannelByNameAndCurrency(@Param("cnName") String cnName,@Param("currency") String currency);

    /**
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 分页查询通道
     * @return
     **/
    List<Channel> pageFindChannel(ChannelDTO channelDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 根据通道id查取详情
     **/
    ChanProVO getChannelById(@Param("channelId") String channelId,@Param("language") String language);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 根据产品id查取通道
     **/
    List<Channel> getChannelByProductId(String channelId);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 根据机构Id和产品Id查询未添加通道
     **/
    List<Channel> getChannelByInsIdAndProId(@Param("institutionId") String institutionId,@Param("productId") String productId);

    /**
     * 通道导出查询
     * @param channelDTO
     * @return
     */
    List<ChannelExportVO> exportAllChannels(ChannelExportDTO channelDTO);


    List<Channel> getAllChannel();
}
