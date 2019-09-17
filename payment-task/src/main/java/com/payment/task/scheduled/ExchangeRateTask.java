package com.payment.task.scheduled;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.entity.ExchangeRate;
import com.payment.common.redis.RedisService;
import com.payment.common.utils.IDS;
import com.payment.common.vo.ExchangeRateVO;
import com.payment.task.dao.DictionaryMapper;
import com.payment.task.dao.ExchangeRateMapper;
import com.payment.task.feign.MessageFeign;
import com.payment.task.utils.JsoupUtill;
import com.payment.task.vo.ExchangeRateScheduledVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 获取中国人民银行的汇率已废除
 */
@Component
@Slf4j
@Api(value = "爬取汇率定时任务")
public class ExchangeRateTask {

    @Autowired
    private ExchangeRateMapper exchangeRateMapper;

    @Autowired
    private DictionaryMapper dictionaryMapper;

    @Autowired
    private MessageFeign messageFeign;

    @Autowired
    private RedisService redisService;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    /**
     * 爬取中国银行人民币汇率信息
     * cron = 秒，分，时，日，月，星期，年(可留空)
     * 每天早上9点开始到9点59结束,每分钟执行一次
     * 2019-08-05 暂停使用
     */
    //@Scheduled(cron = "0/10 * * * * ? ")//每10秒执行一次 测试用
    //@Scheduled(cron = "0 * 9 ? * *")
    @Transactional
    public void getBocExchangeRate() {
        try {
            //判断当日的汇率信息是否存在
            Date nowDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            List<ExchangeRateVO> lists = exchangeRateMapper.selectByCreateTimeAndCreator(sdf.format(nowDate), "中国银行-人民币");
            if (lists != null && lists.size() != 0) {
                return;
            }
            sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
            //利用爬虫抓取外部官方网站当日汇率
            log.info("**************************************开始执行爬取中国银行网址上的汇率爬取任务********************************************");
            List<Element> exRateList = JsoupUtill.getBocExRateNodeList(JsoupUtill.getPageDocument("http://www.boc.cn/sourcedb/whpj/enindex_4.html"));
            //未查询到汇率信息
            if (exRateList == null || exRateList.size() == 0) {
                messageFeign.sendSimple(developerMobile, "爬虫获取汇率数据异常!");
                messageFeign.sendSimpleMail(developerEmail, "爬虫获取汇率数据异常", "爬虫获取汇率数据异常");
                log.info("爬虫获取汇率数据异常!,今日时间:{}", sdf.format(nowDate));
                return;
            }
            log.info("***************************************结束爬取中国银行网址上的汇率爬取任务***********************************************");
            //判断当前查询到的汇率发布时间是否在每日9点之后
            Element ele = exRateList.get(0);
            String[] exRate = JsoupUtill.getText(ele.html()).split(" ");
            String[] split = exRate[6].split("&");
            //汇率日期
            String exRateDate = split[0];
            //汇率时刻
            String exRateTime = exRate[7];
            sdf.applyPattern("yyyy-MM-dd");
            String todayDate = sdf.format(nowDate);
            //查询到的汇率日期与当前日期不同
            if (!exRateDate.equals(todayDate)) {
                return;
            }
            String exTime = exRateTime.substring(0, 2);//汇率发布时刻
            int exHour = Integer.parseInt(exTime);
            //查询到的汇率时刻在9点之前
            if (exHour < 9) {
                return;
            }
            //获取系统的支持的币种
            List<String> enableList = dictionaryMapper.getCodeWithDicTypeCode(AsianWalletConstant.CURRENCY_CODE, TradeConstant.ZH_CN);
            //未查询到预设币种
            if (enableList == null || enableList.size() == 0) {
                log.info("未获取到预设币种信息!");
                return;
            }
            Map<String, ExchangeRateScheduledVO> currencyMap = new HashMap<>(); //币种代码 对应的 买入汇率与卖出汇率与发布时间
            //存储预设币种与对应的买入汇率,卖出汇率与发布时间
            for (Element node : exRateList) {
                String nodeText = JsoupUtill.getText(node.html()); //取出每个节点的文本信息
                String[] nodeArray = nodeText.split(" ");
                for (String enableName : enableList) {
                    if (enableName.equals(nodeArray[0])) {
                        String[] splits = nodeArray[6].split("&"); //发布日期
                        String buyRate = nodeArray[1];//买入汇率
                        String saleRate = nodeArray[3];//卖出汇率
                        String hourMillSeconds = nodeArray[7];//发布时间的时分秒
                        String date = splits[0];//发布时间日期
                        ExchangeRateScheduledVO exchangeRateVO = new ExchangeRateScheduledVO(buyRate, saleRate, date + " " + hourMillSeconds);
                        currencyMap.put(enableName, exchangeRateVO);
                    }
                }
            }
            //汇率信息落地
            List<ExchangeRate> exchangeRates = new ArrayList<>();//汇率实体集合
            Set<String> currencyCodeSet = currencyMap.keySet();//预设币种代码
            for (String currencyCode : currencyCodeSet) {
                ExchangeRateScheduledVO exchangeRateScheduledVO = currencyMap.get(currencyCode);//获取币种代码对应的信息
                //买入汇率不为空才添加
                if (!StringUtils.isEmpty(exchangeRateScheduledVO.getBuyRate())) {
                    ExchangeRate r = new ExchangeRate();
                    r.setId(IDS.uuid2());
                    r.setBuyRate(new BigDecimal(exchangeRateScheduledVO.getBuyRate()).divide(new BigDecimal(100)));//买入汇率
                    if (!StringUtils.isEmpty(exchangeRateScheduledVO.getSaleRate())) {
                        r.setSaleRate(new BigDecimal(exchangeRateScheduledVO.getSaleRate()).divide(new BigDecimal(100)));//卖出汇率
                    }
                    r.setEnabled(true);
                    r.setLocalCurrency(currencyCode);
                    r.setForeignCurrency("CNY");
                    sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
                    r.setUsingTime(sdf.parse(exchangeRateScheduledVO.getUsingTime())); //发布时间
                    r.setCreateTime(new Date());
                    r.setCreator("中国银行-人民币");
                    r.setModifier(null);
                    r.setRemark(null);
                    exchangeRates.add(r);
                    //禁用已经取到的相同币种的汇率信息
                    exchangeRateMapper.updateStatusByLocalCurrencyAndForeignCurrency(currencyCode, "CNY", "爬虫");
                }
            }
            exchangeRateMapper.insertList(exchangeRates);
            //将汇率信息同步到redis
            try {
                for (ExchangeRate exchangeRate : exchangeRates) {
                    redisService.set(AsianWalletConstant.EXCHANGERATE_CACHE_KEY.concat("_").concat(exchangeRate.getLocalCurrency()).concat("_").
                            concat(exchangeRate.getForeignCurrency()), JSON.toJSONString(exchangeRate));
                }
            } catch (Exception e) {
                log.error("汇率定时任务job获取汇率信息同步到redis里发生错误:", e);
            }
        } catch (Exception e) {
            log.error("爬取汇率定时任务发生异常:", e);
        }
    }

    /**
     * 爬取越南盾汇率信息
     * cron = 秒，分，时，日，月，星期，年(可留空)
     * 每天早上9点开始到9点59结束,每分钟执行一次
     * 2019-08-05 暂停使用
     */
//    @Scheduled(cron = "0/10 * * * * ? ")//每10秒执行一次 测试用
//    @Scheduled(cron = "0 * 9 ? * *")
    @Transactional
    public void getVndExchangeRate() {
        try {
            //判断当日的汇率信息是否存在
            Date nowDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            List<ExchangeRateVO> lists = exchangeRateMapper.selectByCreateTimeAndCreator(sdf.format(nowDate), "中国银行-越南盾");
            if (lists != null && lists.size() != 0) {
                return;
            }
            sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
            //利用爬虫抓取外部官方网站当日汇率
            List<Element> exRateList = JsoupUtill.getVndExRateList(JsoupUtill.getPageDocument("http://www.bankofchina.com/sourcedb/vnd/"));
            //未查询到汇率信息
            if (exRateList == null || exRateList.size() == 0) {
                messageFeign.sendSimple(developerMobile, "爬虫获取汇率数据异常!");
                messageFeign.sendSimpleMail(developerEmail, "爬虫获取汇率数据异常", "爬虫获取汇率数据异常");
                log.info("爬虫获取汇率数据异常!,今日时间:{}", sdf.format(nowDate));
                return;
            }
            //获取系统的支持的币种
            List<String> enableList = dictionaryMapper.getCodeWithDicTypeCode(AsianWalletConstant.CURRENCY_CODE, TradeConstant.ZH_CN);
            //未查询到预设币种
            if (enableList == null || enableList.size() == 0) {
                log.info("未获取到预设币种信息!");
                return;
            }
            Map<String, ExchangeRateScheduledVO> currencyMap = new HashMap<>(); //币种代码 对应的 买入汇率与卖出汇率与发布时间
            //存储预设币种与对应的买入汇率,卖出汇率与发布时间
            for (Element node : exRateList) {
                String nodeText = JsoupUtill.getText(node.html()); //取出每个节点的文本信息
                String[] nodeArray = nodeText.split(" ");
                for (String enableName : enableList) {
                    if (enableName.equals(nodeArray[0])) {
                        String[] splits = nodeArray[5].split("&"); //发布日期
                        String buyRate = nodeArray[1];//买入汇率
                        String saleRate = nodeArray[2];//卖出汇率
                        String hourMillSeconds = nodeArray[6];//发布时间的时分秒
                        String date = splits[0];//发布时间日期
                        ExchangeRateScheduledVO exchangeRateVO = new ExchangeRateScheduledVO(buyRate, saleRate, date + " " + hourMillSeconds);
                        if (nodeArray[0].equals("USD")) {
                            String remark = nodeArray[7];//备注
                            exchangeRateVO.setRemark(remark);
                        }
                        currencyMap.put(enableName, exchangeRateVO);
                    }
                }
            }
            //汇率信息落地
            List<ExchangeRate> exchangeRates = new ArrayList<>();//汇率实体集合
            Set<String> currencyCodeSet = currencyMap.keySet();//预设币种代码
            for (String currencyCode : currencyCodeSet) {
                ExchangeRateScheduledVO exchangeRateScheduledVO = currencyMap.get(currencyCode);//获取币种代码对应的信息
                //买入汇率不为空才添加
                if (!StringUtils.isEmpty(exchangeRateScheduledVO.getBuyRate())) {
                    ExchangeRate r = new ExchangeRate();
                    r.setId(IDS.uuid2());
                    r.setBuyRate(new BigDecimal(exchangeRateScheduledVO.getBuyRate()));//买入汇率
                    r.setSaleRate(new BigDecimal(exchangeRateScheduledVO.getSaleRate()));//卖出汇率
                    r.setEnabled(true);
                    r.setLocalCurrency(currencyCode);
                    r.setForeignCurrency("VND");
                    sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
                    r.setUsingTime(sdf.parse(exchangeRateScheduledVO.getUsingTime())); //发布时间
                    r.setCreateTime(new Date());
                    r.setCreator("中国银行-越南盾");
                    r.setModifier(null);
                    r.setRemark(exchangeRateScheduledVO.getRemark());
                    exchangeRates.add(r);
                    //禁用已经取到的相同币种的汇率信息
                    exchangeRateMapper.updateStatusByLocalCurrencyAndForeignCurrency(currencyCode, "VND", "爬虫");
                }
            }
            exchangeRateMapper.insertList(exchangeRates);
            //将汇率信息同步到redis
            try {
                for (ExchangeRate exchangeRate : exchangeRates) {
                    redisService.set(AsianWalletConstant.EXCHANGERATE_CACHE_KEY.concat("_").concat(exchangeRate.getLocalCurrency()).concat("_").
                            concat(exchangeRate.getForeignCurrency()), JSON.toJSONString(exchangeRate));
                }
            } catch (Exception e) {
                log.error("汇率定时任务job获取汇率信息同步到redis里发生错误:", e);
            }
        } catch (Exception e) {
            //爬取异常
            log.error("爬取汇率定时任务发生异常:", e);
        }
    }
}


