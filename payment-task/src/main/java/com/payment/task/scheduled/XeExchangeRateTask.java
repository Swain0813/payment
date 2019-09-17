package com.payment.task.scheduled;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.entity.ExchangeRate;
import com.payment.common.redis.RedisService;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.HttpClientUtils;
import com.payment.common.utils.IDS;
import com.payment.task.dao.ExchangeRateMapper;
import com.payment.task.feign.MessageFeign;
import com.payment.task.vo.XeRateResponseVO;
import com.payment.task.vo.XeRateVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * Xe汇率API调用定时任务
 * 已经废除
 */
@Component
@Slf4j
@Api(value = "Xe汇率API调用定时任务")
public class XeExchangeRateTask {

    @Autowired
    private ExchangeRateMapper exchangeRateMapper;

    @Autowired
    private MessageFeign messageFeign;

    @Autowired
    private RedisService redisService;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    @Value("${custom.xe.accountId}")
    private String accountId;

    @Value("${custom.xe.apiKey}")
    private String apiKey;


    /**
     * Xe汇率查询定时任务
     * 每天早上6点开始执行,执行一次
     * 暂停使用8月31日
     */
    //@Scheduled(cron = "0 0 9 ? * *")
    //@Scheduled(cron = "0/10 * * * * ? ")//每10秒执行一次 测试用
    @Transactional(rollbackFor = Exception.class)
    public void getXeRate() {
        log.info("=============【XE汇率调用定时任务】=============开始执行");
        Map<String, List<String>> currencyMap = getCurrencyMap();
        //Xe汇率查询接口URL
        String convertRateUrl = "https://xecdapi.xe.com/v1/convert_from/";
        //请求头Map
        Map<String, Object> headerMap = new HashMap<>();
        Base64 base64 = new Base64();
        //将账户id与Api-Key使用Base64加密
        String authorization = base64.encodeAsString((accountId + ":" + apiKey).getBytes());
        headerMap.put("Authorization", "Basic " + authorization);
        try {
            for (String from : currencyMap.keySet()) {
                StringBuilder sb = new StringBuilder();
                //拼接XE汇率查询接口的URL
                sb.append(convertRateUrl).append("?from=").append(from).append("&to=");
                List<String> toList = currencyMap.get(from);
                for (int i = 0; i < toList.size(); i++) {
                    if (i == toList.size() - 1) {
                        sb.append(toList.get(i));
                    } else {
                        sb.append(toList.get(i)).append(",");
                    }
                }
                String queryUrl = sb.toString();
                log.info("=============【XE汇率调用定时任务】=============【查询URL记录】 queryUrl: {}", queryUrl);
                //调用XE汇率查询接口
                String xeResponse = HttpClientUtils.reqGetString(queryUrl, null, headerMap);
                log.info("=============【XE汇率调用定时任务】=============【响应参数记录】 xeResponse: {}", xeResponse);
                XeRateVO xeRateVO = JSONObject.parseObject(xeResponse, XeRateVO.class);
                if (xeRateVO == null || xeRateVO.getTo() == null || xeRateVO.getTo().size() == 0) {
                    log.info("=============【XE汇率调用定时任务】=============【响应结果为空】");
                    messageFeign.sendSimple(developerMobile, "XE汇率定时任务获取数据异常!【本位币种】: " + from);
                    messageFeign.sendSimpleMail(developerEmail, "XE汇率定时任务获取数据异常! ", "XE汇率定时任务获取数据异常!【本位币种】: " + from);
                    break;
                }
                log.info("=============【XE汇率调用定时任务】=============JSON解析后的【响应参数记录】 xeRateVO: {}", JSON.toJSONString(xeRateVO));
                List<XeRateResponseVO> rateList = xeRateVO.getTo();
                //汇率实体集合
                List<ExchangeRate> exchangeRates = new ArrayList<>();
                for (XeRateResponseVO xeRateResponseVO : rateList) {
                    //汇率不为null才添加
                    if (xeRateResponseVO.getMid() != null) {
                        ExchangeRate exchangeRate = new ExchangeRate();
                        exchangeRate.setId(IDS.uuid2());
                        exchangeRate.setLocalCurrency(xeRateVO.getFrom());
                        exchangeRate.setForeignCurrency(xeRateResponseVO.getQuotecurrency());
                        exchangeRate.setBuyRate(xeRateResponseVO.getMid());
                        exchangeRate.setUsingTime(DateToolUtils.getDateFromStringTz(xeRateVO.getTimestamp()));
                        exchangeRate.setCreateTime(new Date());
                        exchangeRate.setCreator("XE定时任务");
                        exchangeRate.setEnabled(true);
                        exchangeRates.add(exchangeRate);
                        //禁用已启用的对应币种的汇率信息
                        exchangeRateMapper.updateStatusByLocalCurrencyAndForeignCurrency(xeRateVO.getFrom(), xeRateResponseVO.getQuotecurrency(), "XE定时任务");
                    }
                }
                exchangeRateMapper.insertList(exchangeRates);
                //将汇率信息同步到redis
                try {
                    for (ExchangeRate exchangeRate : exchangeRates) {
                        redisService.set(AsianWalletConstant.EXCHANGERATE_CACHE_KEY.concat("_").concat(exchangeRate.getLocalCurrency()).concat("_")
                                .concat(exchangeRate.getForeignCurrency()), JSON.toJSONString(exchangeRate));
                    }
                } catch (Exception e) {
                    log.error("=============【XE汇率调用定时任务】=============同步Redis发生错误", e);
                }
            }
        } catch (Exception e) {
            log.error("=============【XE汇率调用定时任务】=============【定时任务发生异常】", e);
            messageFeign.sendSimple(developerMobile, "XE汇率定时任务获取数据异常!");
            messageFeign.sendSimpleMail(developerEmail, "XE汇率定时任务获取数据异常!", "XE汇率定时任务获取数据异常!");
        }
        log.info("=============【XE汇率调用定时任务】=============结束执行");
    }


    /**
     * 获取币种对应关系
     *
     * @return 币种对应关系Map
     */
    private Map<String, List<String>> getCurrencyMap() {
        List<String> currencyList = new ArrayList<>();
        currencyList.add("USD");
        currencyList.add("SGD");
        currencyList.add("MYR");
        currencyList.add("IDR");
        currencyList.add("AUD");
        currencyList.add("VND");
        currencyList.add("THB");
        currencyList.add("HKD");
        Map<String, List<String>> currencyMap = new HashMap<>();
        for (String code : currencyList) {
            List<String> tempList = new ArrayList<>();
            for (String currency : currencyList) {
                if (!code.equals(currency)) {
                    tempList.add(currency);
                }
            }
            currencyMap.put(code, tempList);
        }
        return currencyMap;
    }

}
