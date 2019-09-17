package com.payment.channels.service.impl;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.payment.channels.config.ChannelsConfig;
import com.payment.channels.dao.ChannelsOrderMapper;
import com.payment.channels.service.NganLuongService;
import com.payment.common.constant.AD3Constant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.nganluong.NganLuongDTO;
import com.payment.common.entity.ChannelsOrder;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.BeanToMapUtil;
import com.payment.common.utils.XMLUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @description: NganLuong通道
 * @author: YangXu
 * @create: 2019-06-18 11:13
 **/
@Service
@Slf4j
public class NganLuongServiceImpl implements NganLuongService {

    @Autowired
    private ChannelsConfig channelsConfig;

    @Autowired
    private ChannelsOrderMapper channelsOrderMapper;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/5/28
     * @Descripate NganLuong收单接口
     **/
    @Override
    public BaseResponse nganLuongPay(NganLuongDTO nganLuongDTO) {
        int num = channelsOrderMapper.selectCountById(nganLuongDTO.getNganLuongRequestDTO().getOrder_code());
        ChannelsOrder co;
        if (num > 0) {
            co = channelsOrderMapper.selectByPrimaryKey(nganLuongDTO.getNganLuongRequestDTO().getOrder_code());
        } else {
            co = new ChannelsOrder();
        }
        co.setInstitutionOrderId(nganLuongDTO.getInstitutionOrderId());
        co.setTradeCurrency("VND");
        co.setTradeAmount(new BigDecimal(nganLuongDTO.getNganLuongRequestDTO().getTotal_amount()));
        co.setReqIp(nganLuongDTO.getReqIp());
        co.setDraweeName(nganLuongDTO.getNganLuongRequestDTO().getBuyer_fullname());
        co.setDraweeEmail(nganLuongDTO.getNganLuongRequestDTO().getBuyer_email());
        co.setBrowserUrl(nganLuongDTO.getNganLuongRequestDTO().getReturn_url());
        //co.setServerUrl("");
        co.setDraweePhone(nganLuongDTO.getNganLuongRequestDTO().getBuyer_mobile());
        co.setTradeStatus(TradeConstant.TRADE_WAIT);
        co.setIssuerId(nganLuongDTO.getNganLuongRequestDTO().getBank_code());
        co.setMd5KeyStr(nganLuongDTO.getNganLuongRequestDTO().getMerchant_password());
        co.setId(nganLuongDTO.getNganLuongRequestDTO().getOrder_code());
        co.setOrderType(AD3Constant.TRADE_ORDER.toString());
        if (num > 0) {
            co.setUpdateTime(new Date());
            channelsOrderMapper.updateByPrimaryKeySelective(co);
        } else {
            co.setCreateTime(new Date());
            channelsOrderMapper.insert(co);
        }

        BaseResponse response = new BaseResponse();
        log.info("-----------------NganLuong收单接口----------------- nganLuongDTO:{} getNganLuongPayUrl:{}", JSON.toJSONString(nganLuongDTO),channelsConfig.getNganLuongPayUrl());
        long start = System.currentTimeMillis();
        cn.hutool.http.HttpResponse execute = HttpRequest.post(channelsConfig.getNganLuongPayUrl())
                .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .form(BeanToMapUtil.beanToMap(nganLuongDTO.getNganLuongRequestDTO()))
                .timeout(20000)
                .execute();
        long end = System.currentTimeMillis();
        log.info("-------NganLuong收单接口-------Time:{} MS", (end - start));
        int status = execute.getStatus();
        //判断HTTP状态码
        if (status != AsianWalletConstant.HTTP_SUCCESS_STATUS) {
            log.info("----------------------向上游接口发送订单失败日志记录----------------------http状态码:{},nganLuongRequestDTO:{}", status, JSON.toJSON(nganLuongDTO.getNganLuongRequestDTO()));
            response.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return response;
        }
        String body = execute.body();
        log.info("----------------------NganLuong收单接口返回----------------------转换前的xml body:{}", body);
        // 注解方式xml转换为map对象
        if (StringUtils.isEmpty(body)) {
            response.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return response;
        }
        try {
            Map<String, String> map = XMLUtil.xmlToMap(body, "UTF-8");
            log.info("----------------------NganLuong收单接口----------------------转换后的 map:{}", map);
            response.setData(map);
        } catch (Exception e) {
            log.info("----------------------NganLuong收单接口 xml 转换异常 ----------------------body:{}", body);
        }
        return response;
    }
}
