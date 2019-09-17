package com.payment.channels;

import com.alibaba.fastjson.JSON;
import com.payment.channels.service.Help2PayService;
import com.payment.channels.service.WechatService;
import com.payment.common.dto.help2pay.Help2PayOutDTO;
import com.payment.common.dto.wechat.WechaRefundDTO;
import com.payment.common.dto.wechat.WechatQueryDTO;
import com.payment.common.entity.OrderRefund;
import com.payment.common.response.BaseResponse;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.IDS;
import com.payment.common.utils.UUIDHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Date;

import static com.alibaba.fastjson.JSON.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ChannelsApplicationTests extends SpringBootServletInitializer {

    @Autowired
    private WechatService wechatService;

    @Autowired
    private Help2PayService help2PayService;


    @Test
    public void contextLoads() {

        WechaRefundDTO wechaRefundDTO = new WechaRefundDTO();
        wechaRefundDTO.setApikey("QWERTY2580lkjhgf1649ZXC203mnb742");
        wechaRefundDTO.setAppid("wx14e049b9320bccca");
        //wechaRefundDTO.setSub_appid("");
        wechaRefundDTO.setSign_type("MD5");
        wechaRefundDTO.setMch_id("1500662841");
        wechaRefundDTO.setSub_mch_id("290476699");
        wechaRefundDTO.setNonce_str(IDS.uuid2());
        wechaRefundDTO.setRefund_account("REFUND_SOURCE_UNSETTLED_FUNDS");
        wechaRefundDTO.setTotal_fee(new BigDecimal(1).multiply(new BigDecimal(100)).intValue());
        wechaRefundDTO.setRefund_fee(new BigDecimal(1).multiply(new BigDecimal(100)).intValue());
        wechaRefundDTO.setRefund_fee_type("USD");
        wechaRefundDTO.setRefund_desc("tk");
        wechaRefundDTO.setTransaction_id("4200000344201907154523934534");
        wechaRefundDTO.setOut_trade_no("CBO201907151507277");
        wechaRefundDTO.setOut_refund_no(IDS.uuid2());

        wechatService.wechatRefund(wechaRefundDTO);

    }

    @Test
    public void contextLoads1() {

        WechatQueryDTO wechatQueryDTO = new WechatQueryDTO();
        wechatQueryDTO.setAppid("wx14e049b9320bccca");
        wechatQueryDTO.setSign_type("MD5");
        wechatQueryDTO.setMch_id("1500662841");
        wechatQueryDTO.setSub_mch_id("290476699");
        wechatQueryDTO.setNonce_str(UUIDHelper.getRandomString(32));
        wechatQueryDTO.setOut_trade_no("CBO201907151507277");
        wechatQueryDTO.setMd5KeyStr("QWERTY2580lkjhgf1649ZXC203mnb742");

        wechatService.wechatQuery(wechatQueryDTO);
        //wechatQueryDTO.setAppid("wx14e049b9320bccca");
        //wechatQueryDTO.setSign_type("MD5");
        //wechatQueryDTO.setMch_id("1488514432");
        //wechatQueryDTO.setSub_mch_id("66104046");
        //wechatQueryDTO.setNonce_str( UUIDHelper.getRandomString(32));
        //wechatQueryDTO.setOut_trade_no("CBO201907151507277");
        //wechatQueryDTO.setMd5KeyStr("VwifO1ailf4jzn0Gsio0angM0fpva2N9");
        //wechatService.wechatQuery(wechatQueryDTO);

    }

    @Test
    public void contextLoads2() {
        Help2PayOutDTO help2PayOutDTO = new Help2PayOutDTO();
        //help2PayOutDTO.setClientIP("192.168.124.28");
        help2PayOutDTO.setClientIP("119.23.136.80");
        help2PayOutDTO.setReturnURI("https://hao.360.com");
        help2PayOutDTO.setMerchantCode("M0285");
        help2PayOutDTO.setTransactionID(IDS.uuid2());
        help2PayOutDTO.setCurrencyCode("MYR");
        help2PayOutDTO.setMemberCode("11111");
        help2PayOutDTO.setAmount("100");
        help2PayOutDTO.setTransactionDateTime(DateToolUtils.LONG_DATE_FORMAT_AA.format(new Date()));
        help2PayOutDTO.setBankCode("CIMB");
        help2PayOutDTO.setToBankAccountName("CIMB Bank");
        help2PayOutDTO.setToBankAccountNumber("11111");
        help2PayOutDTO.setToProvince("");
        help2PayOutDTO.setToCity("");
        help2PayOutDTO.setSecurityCode("WbRsYjndLf2FKQH");
        BaseResponse baseResponse = help2PayService.help2PayOut(help2PayOutDTO);
    }

}

