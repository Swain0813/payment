package com.payment.institution.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.payment.common.base.BaseServiceImpl;
import com.payment.common.config.AuditorProvider;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.ChannelDTO;
import com.payment.common.dto.ChannelExportDTO;
import com.payment.common.entity.Channel;
import com.payment.common.entity.ChannelBank;
import com.payment.common.entity.Product;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.IDS;
import com.payment.common.vo.ChanProVO;
import com.payment.common.vo.ChannelExportVO;
import com.payment.institution.dao.*;
import com.payment.institution.entity.ProductChannel;
import com.payment.institution.service.ChannelService;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description: 通道Service
 * @author: YangXu
 * @create: 2019-01-30 14:24
 **/
@Service
@Transactional
@Slf4j
public class ChannelServiceImpl extends BaseServiceImpl<Channel> implements ChannelService {

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private ProductChannelMapper productChannelMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AuditorProvider auditorProvider;

    @Autowired
    private ChannelBankMapper channelBankMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 添加通道信息
     **/
    @Override
    public int addChannel(String creator, ChannelDTO channelDTO) {
        //校验产品币种与通道币种是否一致
        for (String productId : channelDTO.getProductId()) {
            Product product = productMapper.selectByPrimaryKey(productId);
            if (!channelDTO.getCurrency().equals(product.getCurrency())) {
                //通道与产品币种不一致
                throw new BusinessException(EResultEnum.PRODUCT_CHANNEL_CURRENCY_NO_SAME.getCode());
            }
        }
        int num;
        //保存数据
        Channel channel = new Channel();
        String channelId = IDS.uuid2();
        BeanUtils.copyProperties(channelDTO, channel);
        channel.setLimitMinAmount(BigDecimal.valueOf(Double.parseDouble(channelDTO.getLimitMinAmount())));
        channel.setLimitMaxAmount(BigDecimal.valueOf(Double.parseDouble(channelDTO.getLimitMaxAmount())));
        channel.setId(channelId);
        channel.setCurrency(channelDTO.getCurrency());
        channel.setChannelCode(IDS.uniqueID().toString());
        //添加通道信息是否存在
        if (channelMapper.getChannelByNameAndCurrency(channelDTO.getChannelCnName(), channelDTO.getCurrency()) > 0) {
            throw new BusinessException(EResultEnum.REPEATED_ADDITION.getCode());
        }
        //通道手续费类型为单笔定额时,最大最小费率设为null
        if (channelDTO.getChannelFeeType() != null && TradeConstant.FEE_TYPE_QUOTA.equals(channelDTO.getChannelFeeType())) {
            channel.setChannelMinRate(null);//通道手续费最小值
            channel.setChannelMaxRate(null);//通道手续费最大值
        }
        //通道网关手续费类型为单笔定额时,最大最小费率设为null
        if (channelDTO.getChannelGatewayFeeType() != null && TradeConstant.FEE_TYPE_QUOTA.equals(channelDTO.getChannelGatewayFeeType())) {
            channel.setChannelGatewayMinRate(null);//通道网关手续费最小值
            channel.setChannelGatewayMaxRate(null);//通道网关手续费最大值
        }
        channel.setCreator(creator);
        channel.setCreateTime(new Date());
        List<ProductChannel> list = Lists.newArrayList();
        for (String productId : channelDTO.getProductId()) {
            ProductChannel productChannel = new ProductChannel();
            productChannel.setId(IDS.uuid2());
            productChannel.setProductId(productId);
            productChannel.setChannelId(channelId);
            productChannel.setCreator(creator);
            productChannel.setCreateTime(new Date());
            list.add(productChannel);
        }
        List<ChannelBank> list1 = Lists.newArrayList();
        for (String bankId : channelDTO.getBankID()) {
            ChannelBank channelBank = new ChannelBank();
            channelBank.setId(IDS.uuid2());
            channelBank.setBankId(bankId);
            channelBank.setChannelId(channelId);
            channelBank.setCreator(creator);
            channelBank.setCreateTime(new Date());
            channelBank.setEnabled(true);
            list1.add(channelBank);
        }
        //新增通道时，产品名称为null的场合
        if (list == null || list.size() == 0 || list1 == null || list1.size() == 0) {
            throw new BusinessException(EResultEnum.PRODUCT_ID_IS_NOT_NULL.getCode());//产品id不能为空
        }
        productChannelMapper.insertList(list);
        channelBankMapper.insertList(list1);
        num = channelMapper.insert(channel);
        try {
            //新增通道信息后添加的redis里
            redisService.set(AsianWalletConstant.CHANNEL_CACHE_KEY.concat("_").concat(channel.getId()), JSON.toJSONString(channel));
            redisService.set(AsianWalletConstant.CHANNEL_CACHE_CODE_KEY.concat("_").concat(channel.getChannelCode()), JSON.toJSONString(channel));
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.ERROR_REDIS_UPDATE.getCode());
        }
        return num;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 修改通道信息
     **/
    @Override
    public int updateChannel(String modifier, ChannelDTO channelDTO) {
        //校验产品币种与通道币种是否一致
        for (String productId : channelDTO.getProductId()) {
            Product product = productMapper.selectByPrimaryKey(productId);
            if (!channelDTO.getCurrency().equals(product.getCurrency())) {
                //通道与产品币种不一致
                throw new BusinessException(EResultEnum.PRODUCT_CHANNEL_CURRENCY_NO_SAME.getCode());
            }
        }
        int num;
        log.info("----------------- 修改通道产品关联信息 ----------------modifier : {}，channelDTO : {} ", modifier, JSONObject.toJSONString(channelDTO));
        //查询通道表
        Channel channel = channelMapper.selectByPrimaryKey(channelDTO.getChannelId());
        if (channel == null) {
            throw new BusinessException(EResultEnum.CHANNEL_IS_NOT_EXISTS.getCode());
        }
        BeanUtils.copyProperties(channelDTO, channel);
        if (StringUtils.isEmpty(channelDTO.getLimitMinAmount())) {
            channel.setLimitMinAmount(BigDecimal.ZERO);
        } else {
            channel.setLimitMinAmount(new BigDecimal(channelDTO.getLimitMinAmount()));
        }
        if (StringUtils.isEmpty(channelDTO.getLimitMaxAmount())) {
            channel.setLimitMaxAmount(BigDecimal.ZERO);
        } else {
            channel.setLimitMaxAmount(new BigDecimal(channelDTO.getLimitMaxAmount()));
        }
        channel.setUpdateTime(new Date());//修改时间
        channel.setModifier(modifier);//修改人
        channel.setId(channelDTO.getChannelId());//通道id
        //通道手续费类型为单笔定额时,最大最小费率设为null
        if (channelDTO.getChannelFeeType() != null && TradeConstant.FEE_TYPE_QUOTA.equals(channelDTO.getChannelFeeType())) {
            channel.setChannelMinRate(null);//通道手续费最小值
            channel.setChannelMaxRate(null);//通道手续费最大值
        }
        //通道网关手续费类型为单笔定额时,最大最小费率设为null
        if (channelDTO.getChannelGatewayFeeType() != null && TradeConstant.FEE_TYPE_QUOTA.equals(channelDTO.getChannelGatewayFeeType())) {
            channel.setChannelGatewayMinRate(null);//通道网关手续费最小值
            channel.setChannelGatewayMaxRate(null);//通道网关手续费最大值
        }
        num = channelMapper.updateByPrimaryKeySelective(channel);

        productChannelMapper.deleteByChannelId(channelDTO.getChannelId());
        List<ProductChannel> list = Lists.newArrayList();
        for (String productId : channelDTO.getProductId()) {
            ProductChannel productChannel = new ProductChannel();
            productChannel.setId(IDS.uuid2());
            productChannel.setProductId(productId);
            productChannel.setChannelId(channelDTO.getChannelId());
            productChannel.setCreator(modifier);
            productChannel.setCreateTime(new Date());
            list.add(productChannel);
        }
        productChannelMapper.insertList(list);
        /**************************************************** 同步channelbank 数据 ****************************************************************/

        List<ChannelBank> list1 = channelBankMapper.selectByChannelId(channelDTO.getChannelId());//原数据库银行关联数据
        List<ChannelBank> list2 = new ArrayList<>();//需要添加数据
        for (String bankId : channelDTO.getBankID()) {
            boolean flag = true;
            for (ChannelBank cb : list1) {
                if (bankId.equals(cb.getBankId())) {
                    flag = false;
                    list2.add(cb);
                }
            }
            if (flag) {
                ChannelBank channelBank = new ChannelBank();
                channelBank.setId(IDS.uuid2());
                channelBank.setBankId(bankId);
                channelBank.setChannelId(channelDTO.getChannelId());
                channelBank.setCreator(modifier);
                channelBank.setEnabled(true);
                channelBank.setCreateTime(new Date());
                list2.add(channelBank);
            }
        }

        channelBankMapper.deleteByChannelId(channelDTO.getChannelId());
        channelBankMapper.insertList(list2);
        try {
            //新增通道信息后添加的redis里
            Channel channelNew = channelMapper.selectByPrimaryKey(channelDTO.getChannelId());
            redisService.set(AsianWalletConstant.CHANNEL_CACHE_KEY.concat("_").concat(channel.getId()), JSON.toJSONString(channelNew));
            redisService.set(AsianWalletConstant.CHANNEL_CACHE_CODE_KEY.concat("_").concat(channel.getChannelCode()), JSON.toJSONString(channelNew));
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.ERROR_REDIS_UPDATE.getCode());
        }
        return num;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 分页查询通道
     **/
    @Override
    public PageInfo<Channel> pageFindChannel(ChannelDTO channelDTO) {
        return new PageInfo<Channel>(channelMapper.pageFindChannel(channelDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 查询全部通道
     **/
    @Override
    public List<Channel> getAllChannel() {
        return channelMapper.getAllChannel();
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 启用禁用通道
     **/
    @Override
    public int banChannel(String modifier, String channelId, Boolean enabled) {
        int num;
        Channel channel = channelMapper.selectByPrimaryKey(channelId);
        if (channel == null) {
            throw new BusinessException(EResultEnum.CHANNEL_IS_NOT_EXISTS.getCode());
        }
        channel.setEnabled(enabled);
        channel.setModifier(modifier);
        channel.setUpdateTime(new Date());
        num = channelMapper.updateByPrimaryKeySelective(channel);
        try {
            //更新通道信息后添加的redis里
            redisService.set(AsianWalletConstant.CHANNEL_CACHE_KEY.concat("_").concat(channel.getId()), JSON.toJSONString(channel));
            redisService.set(AsianWalletConstant.CHANNEL_CACHE_CODE_KEY.concat("_").concat(channel.getChannelCode()), JSON.toJSONString(channel));
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.ERROR_REDIS_UPDATE.getCode());
        }
        return num;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 根据通道id查取详情
     **/
    @Override
    public ChanProVO getChannelById(String channelId, String language) {
        return channelMapper.getChannelById(channelId, language);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 根据产品id查取通道
     **/
    @Override
    public List<Channel> getChannelByProductId(String productId) {
        return channelMapper.getChannelByProductId(productId);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 根据机构Id和产品Id查询未添加通道
     **/
    @Override
    public List<Channel> getChannelByInsIdAndProId(String institutionId, String productId) {
        return channelMapper.getChannelByInsIdAndProId(institutionId, productId);
    }

    /**
     * 通道导出功能
     *
     * @param channelDTO
     * @return
     */
    @Override
    public List<ChannelExportVO> exportAllChannels(ChannelExportDTO channelDTO) {
        //获取当前请求的语言
        channelDTO.setLanguage(auditorProvider.getLanguage());//设置语言
        return channelMapper.exportAllChannels(channelDTO);
    }
}
