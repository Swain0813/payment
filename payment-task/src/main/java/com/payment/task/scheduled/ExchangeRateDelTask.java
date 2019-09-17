package com.payment.task.scheduled;
import com.payment.task.dao.ExchangeRateMapper;
import com.payment.task.feign.MessageFeign;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 汇率表的数据每周清理一次
 */
@Component
@Slf4j
@Api(value = "汇率表禁用数据的清理")
public class ExchangeRateDelTask {

    @Autowired
    private MessageFeign messageFeign;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    @Autowired
    private ExchangeRateMapper exchangeRateMapper;

    /**
     * 汇率表禁用数据的清理
     * 2019/7/5 废除暂时不清理禁用的汇率
     */
    @Scheduled(cron = "0 0 12 ? * SUN")//每个星期日中午12点
    //@Scheduled(cron = "0/10 * * * * ? ")//每10秒执行一次 测试用
    @Transactional
    public void deleteExchangeRate(){
        log.info("*********开始清理汇率禁用数据********************");
        try {
            exchangeRateMapper.deleteExchangeRate();
        } catch (Exception e) {
            log.error("清理汇率表禁用数据发生异常==={}", e);
            messageFeign.sendSimple(developerMobile, "清理汇率禁用数据发生异常!");
            messageFeign.sendSimpleMail(developerEmail, "清理汇率禁用数据发生异常", "清理汇率禁用数据发生异常");
        }
        log.info("***********结束清理汇率禁用数据******************");
    }
}
