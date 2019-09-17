package com.payment.clearing.scheduled;
import com.payment.clearing.service.ClearService;
import com.payment.clearing.service.DrawService;
import com.payment.clearing.service.SettleService;
import com.payment.clearing.service.ShareBenefitService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @description: 清结算定时任务入口
 * @author: YangXu
 * @create: 2019-07-26 17:46
 **/
@Slf4j
@Component
@Api(value = "清结算定时任务入口")
public class TaskScheduled {

    @Autowired
    private ClearService clearService;

    @Autowired
    private SettleService settleService;

    @Autowired
    private ShareBenefitService shareBenefitService;

    @Autowired
    private DrawService drawService;

    /**
     * @Author YangXu
     * @Date 2019/7/26
     * @Descripate 批次清算定时任务入口
     * @return
     **/
    @Scheduled(cron = "0 0/1 * * * ?")//测试用
//    @Scheduled(cron = "0 0/5 * * * ?")//生产环境配置
    public void ClearForGroupBatch() {
        log.info("************************** 开始执行清算任务 *********************");
        clearService.ClearForGroupBatch();
        log.info("************************** 结束执行清算任务 *********************");
    }

    /**
     * @Author YangXu
     * @Date 2019/7/26
     * @Descripate 批次结算定时任务入口
     * @return
     **/
    @Scheduled(cron = "0 0/2 * * * ?")//测试用
    //@Scheduled(cron = "0 0/11 * * * ?")//生产环境配置
    public void SettlementForBatch() {
        log.info("************************** 开始执行结算任务 *********************");
        settleService.SettlementForBatch();
        log.info("************************** 结束执行结算任务 *********************");
    }

    /**
     * @Author YangXu
     * @Date 2019/7/26
     * @Descripate 批次分润定时任务入口
     * @return
     **/
    //@Scheduled(cron = "0/10 * * * * ? ")//每10秒执行一次 测试用
    @Scheduled(cron = "0 0 0/2 * * ?")//每两小时执行一次 生产环境配置
    public void ShareBenefitForBatch() {
        log.info("************************** 开始执行分润任务 *********************");
        shareBenefitService.ShareBenefitForBatch();
        log.info("************************** 结束执行分润任务 *********************");
    }

    /**
     * 自动提款跑批定时任务
     */
    //@Scheduled(cron = "0/10 * * * * ? ")//每10秒执行一次 测试用
    @Scheduled(cron = "0 0 7 ? * *")//每天早上7点跑批一次 生产环境用
    public void DrawForBatch() {
        log.info("************************** 开始执行自动提款跑批任务 *********************");
        drawService.DrawForBatch();
        log.info("************************** 结束执行自动提款跑批任务 *********************");
    }
}
