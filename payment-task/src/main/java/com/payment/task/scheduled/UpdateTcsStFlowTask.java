package com.payment.task.scheduled;
import com.payment.common.constant.TradeConstant;
import com.payment.common.entity.OrderLogistics;
import com.payment.common.entity.TcsStFlow;
import com.payment.common.redis.RedisService;
import com.payment.task.dao.OrderLogisticsMapper;
import com.payment.task.dao.TcsStFlowMapper;
import com.payment.task.feign.MessageFeign;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 妥投结算跑批更新结算表中应结算日期的任务
 */
@Component
@Slf4j
@Api(value = "妥投结算根据物流信息已签收的状态更新结算表中的应结算日期的定时任务")
public class UpdateTcsStFlowTask {

    @Autowired
    private MessageFeign messageFeign;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;

    @Autowired
    private TcsStFlowMapper tcsStFlowMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 妥投结算根据物流信息已签收的状态更新结算表中的应结算日期的定时任务
     */
    @Scheduled(cron = "0 0 5 ? * *")// 每天早上5点执行一次
    //@Scheduled(cron = "0/10 * * * * ? ")//每10秒执行一次 测试用
    public void updateShouldSTtime(){
        log.info("************开始定时跑批根据物流订单信息中签收状态更新结算表中的应结算日期****************");
        //判断当日是否已经执行了
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        if(redisService.get(TradeConstant.UPDATE_STF_FLAY_KEY.concat("_").concat(sdf1.format(new Date())))!=null){
            return;
        }
        try{
            //获取当日已签收的物流订单信息
            List<OrderLogistics> orderLogistics = orderLogisticsMapper.getOrderLogistics();
            Map<String,OrderLogistics> map = new HashMap<>();
            for(OrderLogistics orderLogistic:orderLogistics){
                map.put(orderLogistic.getReferenceNo(),orderLogistic);//key为系统订单流水号,value为物流订单信息对象
            }
            //查询结算表中为未结算的妥投结算记录
            List<TcsStFlow> tcsStFlows = tcsStFlowMapper.getTcsStFlows(TradeConstant.SHOULD_STIME);
            //统计更新的记录数
            int result =0;
            for(TcsStFlow tcsStFlow:tcsStFlows){
                if(map.containsKey(tcsStFlow.getRefcnceFlow())){
                   //更新结算表的应结算日期
                    tcsStFlow.setShouldSTtime(new Date());//设置成当前的时间
                    tcsStFlow.setRemark("妥投结算跑批更新结算表中应结算日期的任务");
                    result+= tcsStFlowMapper.updateByPrimaryKeySelective(tcsStFlow);//更新结算表
                    if(result>0){
                        log.info("************妥投结算跑批更新结算表中应结算日期的任务更新成功**************",result);
                    }else {
                        log.info("***********妥投结算跑批更新结算表中应结算日期的任务更新失败****************",result);
                    }
                }
            }
        }catch (Exception e){
            log.error("妥投结算根据物流信息已签收的状态更新结算表中的应结算日期的定时任务发生异常==={}", e.getMessage());
            messageFeign.sendSimple(developerMobile, "妥投结算根据物流信息已签收的状态更新结算表中的应结算日期的定时任务发生异常!");
            messageFeign.sendSimpleMail(developerEmail, "妥投结算根据物流信息已签收的状态更新结算表中的应结算日期的定时任务发生异常", "定时跑批更新结算表中应结算日期发生异常");
        }finally {
            //定时跑批生成结算交易完，在redis里记录一下，说明当日已经跑批
            redisService.set(TradeConstant.UPDATE_STF_FLAY_KEY.concat("_").concat(sdf1.format(new Date())),"true",24 * 60 * 60);//24小时后失效
        }
        log.info("************结束定时跑批根据物流订单信息中签收状态更新结算表中的应结算日期****************");
    }
}
