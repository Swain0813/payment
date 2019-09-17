package com.payment.institution.service;

import com.payment.common.base.BaseService;
import com.payment.common.dto.ChannelDTO;
import com.payment.common.dto.ChannelExportDTO;
import com.payment.common.entity.Channel;
import com.payment.common.vo.ChanProVO;
import com.payment.common.vo.ChannelExportVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ChannelService extends BaseService<Channel> {


    /**
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 添加通道
     * @return
     **/
    int addChannel(String creator,ChannelDTO channelDTO);

    /**
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 修改通道产品关联信息
     * @return
     **/
    int updateChannel(String modifier,ChannelDTO channelDTO);

    /**
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 分页查询通道
     * @return
     **/
    PageInfo<Channel> pageFindChannel(ChannelDTO channelDTO);

    /**
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 查询全部通道
     * @return
     **/
    List<Channel> getAllChannel();

    /**
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 启用禁用通道
     * @return
     **/
    int banChannel(String modifier,String channelId, Boolean enabled);

    /**
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 根据通道id查取详情
     * @return
     **/
    ChanProVO getChannelById(String channelId,String language);

    /**
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 根据产品id查取通道
     * @return
     **/
    List<Channel> getChannelByProductId(String productId);

    /**
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 根据机构Id和产品Id查询未添加通道
     * @return
     **/
    List<Channel> getChannelByInsIdAndProId(String institutionId,String productId);

    /**
     * 通道导出功能
     * @param channelDTO
     * @return
     */
    List<ChannelExportVO> exportAllChannels(ChannelExportDTO channelDTO);



}
