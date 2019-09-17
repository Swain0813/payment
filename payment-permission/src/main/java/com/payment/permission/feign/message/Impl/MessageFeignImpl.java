package com.payment.permission.feign.message.Impl;

import com.payment.common.enums.Status;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.message.MessageFeign;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-28 15:46
 **/
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
     * 发送模板邮件
     * @param sendTo
     * @param languageNum
     * @param templateNum
     * @param param
     * @return
     */
    @Override
    public BaseResponse sendTemplateMail(String sendTo, String languageNum, Status templateNum, Map<String, Object> param){
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

}
