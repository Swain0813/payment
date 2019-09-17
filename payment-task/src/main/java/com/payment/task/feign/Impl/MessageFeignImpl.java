package com.payment.task.feign.Impl;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.task.feign.MessageFeign;
import org.springframework.stereotype.Component;

/**
 * 发送邮件和短信服务
 */
@Component
public class MessageFeignImpl implements MessageFeign {

    /**
     * 发送简单邮件
     * @param sendTo
     * @param title
     * @param content
     * @return
     */
    @Override
    public BaseResponse sendSimpleMail(String sendTo, String title, String content) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 国内普通发送
     * @param mobile
     * @param content
     * @return
     */
    @Override
    public BaseResponse sendSimple(String mobile, String content) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
