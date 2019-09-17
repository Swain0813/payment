package com.payment.task.rabbitmq;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.entity.SettleOrder;
import com.payment.common.response.BaseResponse;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.vo.FundChangeVO;
import com.payment.task.dao.SettleOrderMapper;
import com.payment.task.feign.MessageFeign;
import com.payment.task.service.ClearingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

/**
 * 机构结算提款队列
 */
@Component
@Slf4j
public class SettleOrderMQReceive {

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private SettleOrderMapper settleOrderMapper;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private MessageFeign messageFeign;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    /**
     * 机构结算提款失败
     * @param value
     */
    @RabbitListener(queues = "TC_MQ_WD_DL")
    public void processWd(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        log.info("----------------- 机构结算提款队列 TC_MQ_WD_DL---------------- rabbitMassage : {} ", value);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减-1
            SettleOrder settleOrder = JSON.parseObject(rabbitMassage.getValue(), SettleOrder.class);
            FundChangeDTO fundChangeDTO = new FundChangeDTO(settleOrder);
            fundChangeDTO.setShouldDealtime(DateToolUtils.formatTimestamp.format(new Date()));
            BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
            if (cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                if (fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务成功
                    //插入数据到机构结算表
                    settleOrderMapper.insertSelective(settleOrder);
                } else {//业务失败
                    log.info("----------------- 机构结算提款队列 TC_MQ_WD_DL 上报清结算 业务失败 -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.send(AD3MQConstant.TC_MQ_WD_DL, JSON.toJSONString(rabbitMassage));
                }
            } else {//请求失败
                log.info("----------------- 机构结算提款队列 上报队列 TC_MQ_WD_DL 请求失败 -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                rabbitMQSender.send(AD3MQConstant.TC_MQ_WD_DL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "机构结算提款失败 TC_MQ_WD_DL预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "机构结算提款失败 TC_MQ_WD_DL预警 ", "TC_MQ_WD_DL预警 ：{ " + value + " }");//邮件通知
        }
    }
}
