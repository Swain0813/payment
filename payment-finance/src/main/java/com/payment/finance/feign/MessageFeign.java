package com.payment.finance.feign;
import com.payment.common.response.BaseResponse;
import com.payment.finance.feign.Impl.MessageFeignImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 短信和邮件相关业务
 */
@FeignClient(value = "asianwallet-message", fallback = MessageFeignImpl.class)
public interface MessageFeign {

    @ApiOperation(value = "发送简单邮件")
    @PostMapping("/email/sendSimpleMail")
    BaseResponse sendSimpleMail(@RequestParam(value = "sendTo") @ApiParam String sendTo,
                                @RequestParam(value = "title") @ApiParam String title,
                                @RequestParam(value = "content") @ApiParam String content);

    @ApiOperation(value = "国内普通发送")
    @PostMapping("/sms/sendSimple")
    BaseResponse sendSimple(@RequestParam(value = "mobile") @ApiParam String mobile, @RequestParam(value = "content") @ApiParam String content);
}
