package com.payment.message.service.impl;
import com.payment.common.enums.Status;
import com.payment.message.service.EmailService;
import com.payment.message.utils.SendEmailCommon;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * 发送邮件模块
 * Created by yangshanlong@payment.com on 2019/01/23.
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private SendEmailCommon sendEmailCommon;//连接邮件服务相关的配置


    @Autowired
    private Configuration configuration; //freeMarker configuration

    /**
     * 发送简单邮件
     * @param sendTo 收件人地址
     * @param title 邮件标题
     * @param content 邮件内容
     * @return
     */
    @Override
    public boolean sendSimpleMail(String sendTo, String title, String content){
        try {
            Future<Boolean> booleanFuture = sendEmailCommon.send_common(sendTo, title, content);
            return booleanFuture.get();
        } catch (Exception e) {
            log.error("发送邮件出错：{}--{}--{}", sendTo, title, content, e);
        }
        return false;
    }

    /**
     * 发送模板邮件
     * @param sendTo 收件人地址
     * @param languageNum 语言
     * @param templateNum 模板号
     * @param param 邮件模板中的变量
     * @return
     */
    @Override
    public boolean sendTemplateMail(String sendTo,
                                        String languageNum,
                                        Status templateNum, Map<String, Object> param){
        try {
            Template t=configuration.getTemplate(languageNum+"/email/"+templateNum+".ftl");
            if(t==null){
                t = configuration.getTemplate("zh-cn/email/" + templateNum + ".ftl");
            }
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, param);
            String [] conparts=content.split("</span>");
            String subject=conparts[0].replace("<span>","");
            content=conparts[1];
            return sendEmailCommon.send_common(sendTo, subject, content).get();
        } catch (Exception e) {
            log.error("发送邮件出错：{}--{}--{}--{}", sendTo, languageNum, templateNum, param, e);
        }
        return false;
    }
}
