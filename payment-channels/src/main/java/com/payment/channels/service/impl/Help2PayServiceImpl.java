package com.payment.channels.service.impl;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.payment.channels.config.ChannelsConfig;
import com.payment.channels.dao.ChannelsOrderMapper;
import com.payment.channels.service.Help2PayService;
import com.payment.common.constant.AD3Constant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.help2pay.Help2PayOutDTO;
import com.payment.common.dto.help2pay.Help2PayRequestDTO;
import com.payment.common.entity.ChannelsOrder;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.BeanToMapUtil;
import com.payment.common.utils.MD5;
import com.payment.common.utils.XMLUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-06-10 10:49
 **/
@Service
@Slf4j
@Transactional
public class Help2PayServiceImpl implements Help2PayService {

    @Autowired
    private ChannelsConfig channelsConfig;

    @Autowired
    private ChannelsOrderMapper channelsOrderMapper;

    @Override
    public BaseResponse help2Pay(Help2PayRequestDTO help2PayRequestDTO) {
        int num = channelsOrderMapper.selectCountById(help2PayRequestDTO.getReference());
        ChannelsOrder co;
        if (num > 0) {
            co = channelsOrderMapper.selectByPrimaryKey(help2PayRequestDTO.getReference());
        } else {
            co = new ChannelsOrder();
        }
        co.setInstitutionOrderId(help2PayRequestDTO.getInstitutionOrderId());
        co.setTradeCurrency(help2PayRequestDTO.getCurrency());
        co.setTradeAmount(new BigDecimal(help2PayRequestDTO.getAmount()));
        co.setReqIp(help2PayRequestDTO.getReqIp());
        co.setBrowserUrl(help2PayRequestDTO.getFrontURI());
        co.setServerUrl(help2PayRequestDTO.getBackURI());
        co.setTradeStatus(TradeConstant.TRADE_WAIT);
        co.setIssuerId(help2PayRequestDTO.getBank());
        co.setMd5KeyStr(help2PayRequestDTO.getMd5KeyStr());
        co.setId(help2PayRequestDTO.getReference());
        co.setOrderType(AD3Constant.TRADE_ORDER.toString());
        if (num > 0) {
            co.setUpdateTime(new Date());
            channelsOrderMapper.updateByPrimaryKeySelective(co);
        } else {
            co.setCreateTime(new Date());
            channelsOrderMapper.insert(co);
        }

        BaseResponse response = new BaseResponse();
        log.info("-----------------Help2Pay收单接口----------------- Help2PayRequestDTO:{}", JSON.toJSONString(help2PayRequestDTO));
        long start = System.currentTimeMillis();
        cn.hutool.http.HttpResponse execute = HttpRequest.post(channelsConfig.getHelp2PayUrl())
                .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .form(BeanToMapUtil.beanToMap(help2PayRequestDTO))
                .timeout(20000)
                .execute();
        long end = System.currentTimeMillis();
        log.info("-------Help2Pay通道消耗时间-------Time:{} MS", (end - start));
        int status = execute.getStatus();
        //判断HTTP状态码
        if (status != AsianWalletConstant.HTTP_SUCCESS_STATUS) {
            log.info("----------------------向上游接口发送订单失败日志记录----------------------http状态码:{},Help2PayRequestDTO:{}", status, JSON.toJSON(help2PayRequestDTO));
            response.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return response;
        }
        String body = execute.body();
        log.info("----------------------Help2Pay返回body----------------------body:{}", body);
        if (StringUtils.isEmpty(body)) {
            log.info("----------------------向上游接口发送订单失败日志记录----------------------Help2PayRequestDTO:{}", JSON.toJSON(help2PayRequestDTO));
            response.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return response;
        }
        response.setData(body);
        return response;


    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/7/17
     * @Descripate HELP2PAY汇款接口
     **/
    @Override
    public BaseResponse help2PayOut(Help2PayOutDTO help2PayOutDTO) {
        //请求参数
        BaseResponse response = new BaseResponse();
        try {
            help2PayOutDTO.setClientIP(channelsConfig.getHelp2PayOutIP());
            help2PayOutDTO.setKey(createDepositRequestKey(help2PayOutDTO));
            log.info("----------------- HELP2PAY汇款接口 ----------------- help2PayOutDTO:{}", JSON.toJSONString(help2PayOutDTO));
            long start = System.currentTimeMillis();
            log.info("------- HELP2PAY汇款接口消耗时间 -------url:{} ", channelsConfig.getHelp2PayOutUrl());
            cn.hutool.http.HttpResponse execute = HttpRequest.post(channelsConfig.getHelp2PayOutUrl())
                    .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .form(BeanToMapUtil.beanToMap(help2PayOutDTO))
                    .timeout(200000)
                    .execute();
            long end = System.currentTimeMillis();
            log.info("------- HELP2PAY汇款接口消耗时间 -------Time:{} MS", (end - start));
            int status = execute.getStatus();
            //判断HTTP状态码
            if (status != AsianWalletConstant.HTTP_SUCCESS_STATUS) {
                log.info("---------------------- HELP2PAY汇款接口失败日志记录 ----------------------http状态码:{},help2PayOutDTO:{}", status, JSON.toJSON(help2PayOutDTO));
                response.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
                return response;
            }
            String body = execute.body();
            log.info("---------------------- HELP2PAY汇款接口返回body ----------------------body:{}", body);
            if (StringUtils.isEmpty(body)) {
                log.info("----------------------HELP2PAY汇款接口失败日志记录 ----------------------help2PayOutDTO:{}", JSON.toJSON(help2PayOutDTO));
                response.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
                return response;
            }
            //response.setData(body);
            Map<String, String> map = XMLUtil.xmlToMap(body, "UTF-8");
            log.info("----------------------HELP2PAY汇款接口失败日志记录 ----------------------map:{}", JSON.toJSON(map));
            if(map.get("statusCode").equals("000")){
                response.setCode("200");
            }else{
                response.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
                response.setMsg(map.get("message"));
            }
        } catch (Exception e) {
            log.info("----------------------HELP2PAY汇款接口失败日志记录 ----------------------Exception:{}",e);
        }
        return response;

    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/7/17
     * @Descripate 汇款接口生成签名
     **/
    public String createDepositRequestKey(Help2PayOutDTO help2PayOutDTO) {
        log.info("------------------- help2Pay 汇款接口生成签名  ----------------------- help2PayOutDTO：{}", JSON.toJSONString(help2PayOutDTO));
        String key = null;//生成签名后的key
        String origin = "";//签名前的明文字符串
        if (help2PayOutDTO != null) {
            /*
             *MD5({MerchantCode }{TransactionId }{MemberCode }{Amount}{CurrencyCode}{TransactionDatetime}{ToBankAccountNumber }{SecurityCode})
             */
            if (!StringUtils.isEmpty(help2PayOutDTO.getMemberCode())) {
                origin = origin + help2PayOutDTO.getMemberCode();
            }
            if (!StringUtils.isEmpty(help2PayOutDTO.getTransactionID())) {
                origin = origin + help2PayOutDTO.getTransactionID();
            }
            if (!StringUtils.isEmpty(help2PayOutDTO.getMemberCode())) {
                origin = origin + help2PayOutDTO.getMemberCode();
            }
            if (!StringUtils.isEmpty(help2PayOutDTO.getAmount())) {
                origin = origin + help2PayOutDTO.getAmount();
            }
            if (!StringUtils.isEmpty(help2PayOutDTO.getCurrencyCode())) {
                origin = origin + help2PayOutDTO.getCurrencyCode();
            }
            if (!StringUtils.isEmpty(help2PayOutDTO.getTransactionDateTime())) {
                origin = origin + help2PayOutDTO.getTransactionDateTime();
            }
            if (!StringUtils.isEmpty(help2PayOutDTO.getToBankAccountNumber())) {
                origin = origin + help2PayOutDTO.getToBankAccountNumber();
            }
            if (!StringUtils.isEmpty(help2PayOutDTO.getSecurityCode())) {
                origin = origin + help2PayOutDTO.getSecurityCode();
            }
            if (origin != null && !origin.equals("")) {
                origin = origin.trim();
            }
            log.info("------------------- help2Pay 汇款接口生成签名  ----------------------- origin：{}", JSON.toJSONString(origin));
            key = MD5.MD5Encode(origin).toUpperCase();
            log.info("------------------- help2Pay 汇款接口生成签名  ----------------------- key：{}", JSON.toJSONString(key));
        }
        return key;
    }

}
