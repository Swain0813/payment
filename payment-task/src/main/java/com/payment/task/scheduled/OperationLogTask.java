package com.payment.task.scheduled;
import com.payment.task.dao.OperationLogMapper;
import com.payment.task.feign.MessageFeign;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 操作日志的清除任务每个月清理一次
 */
@Component
@Slf4j
@Api(value = "操作日志的清除任务每个月清理一次定时任务")
public class OperationLogTask {

    @Autowired
    private MessageFeign messageFeign;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 操作日志清理的定时任务
     */
    @Scheduled(cron = "0 15 10 15 * ?")//每月15日上午10:15触发
    //@Scheduled(cron = "0/10 * * * * ? ")//每10秒执行一次 测试用
    @Transactional
     public void clearOperLog(){
        log.info("*********开始清理操作日志表的数据********************");
        try {
            operationLogMapper.deleteOperatLog();
        } catch (Exception e) {
            log.error("清理操作日志表的数据发生异常==={}", e);
            messageFeign.sendSimple(developerMobile, "清理操作日志表发生异常!");
            messageFeign.sendSimpleMail(developerEmail, "清理操作日志表发生异常", "清理操作日志表发生异常");
        }
        log.info("***********结束清理操作日志表的数据******************");
     }
}
