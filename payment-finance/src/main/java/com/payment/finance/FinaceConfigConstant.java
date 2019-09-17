package com.payment.finance;

import com.payment.finance.constant.FinaceConstant;
import com.payment.finance.dao.ChannelMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Wu, Hua-Zheng
 * @version v1.0.0
 * @classDesc: 功能描述: 项目配置文件初始化
 * @createTime 2018年8月2日 下午9:21:14
 * @copyright: 上海众哈网络技术有限公司
 */
@Slf4j
@Component
@Order(value = 0)
public class FinaceConfigConstant implements CommandLineRunner {

    @Autowired
    private ChannelMapper channelMapper;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/30
     * @Descripate 加载通道编号
     **/
    @Override
    public void run(String... args) throws Exception {
        List<String> list = channelMapper.getChannelCodeByname("AD3");
        for (String s : list) {
            log.info(">>>>>>>>>>>>>>>加载通道编号 : 【{}】 <<<<<<<<<<<<<", s);
        }
        FinaceConstant.FinaceChannelNameMap.put("AD3", list);
        log.info(">>>>>>>>>>>>>>>加载通道编号;FinaceChannelNameMap : 【{}】 <<<<<<<<<<<<<", FinaceConstant.FinaceChannelNameMap.size());
    }

}
