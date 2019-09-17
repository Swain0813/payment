package com.payment.trade.feign;
import com.payment.common.enums.Status;
import com.payment.common.response.BaseResponse;
import com.payment.trade.feign.Impl.MessageFeignImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 短信和邮件相关业务
 */
@FeignClient(value = "payment-message", fallback = MessageFeignImpl.class)
public interface MessageFeign {

    @ApiOperation(value = "发送简单邮件")
    @PostMapping("/email/sendSimpleMail")
    BaseResponse sendSimpleMail(@RequestParam(value = "sendTo") @ApiParam String sendTo,
                                @RequestParam(value = "title") @ApiParam String title,
                                @RequestParam(value = "content") @ApiParam String content);

    @ApiOperation(value = "国内普通发送")
    @PostMapping("/sms/sendSimple")
    BaseResponse sendSimple(@RequestParam(value = "mobile") @ApiParam String mobile, @RequestParam(value = "content") @ApiParam String content);

    @ApiOperation(value = "发送模板邮件")
    @PostMapping("/email/sendTemplateMail")
    BaseResponse sendTemplateMail(@RequestParam(value = "sendTo") @ApiParam String sendTo, @RequestParam(value = "languageNum") @ApiParam
            String languageNum, @RequestParam(value = "templateNum") @ApiParam Status templateNum, @RequestBody Map<String, Object> param);
}
