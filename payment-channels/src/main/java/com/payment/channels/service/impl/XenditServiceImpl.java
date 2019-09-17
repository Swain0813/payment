package com.payment.channels.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.payment.channels.config.ChannelsConfig;
import com.payment.channels.dao.ChannelsOrderMapper;
import com.payment.channels.service.XenditService;
import com.payment.common.constant.AD3Constant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.xendit.XenditDTO;
import com.payment.common.dto.xendit.XenditPayResDTO;
import com.payment.common.dto.xendit.XenditResDTO;
import com.payment.common.entity.ChannelsOrder;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.HttpResponse;
import com.payment.common.utils.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class XenditServiceImpl implements XenditService {

    @Autowired
    private ChannelsConfig channelsConfig;

    @Autowired
    private ChannelsOrderMapper channelsOrderMapper;

    /**
     * xendit网银收单方法
     *
     * @param xenditDTO xendit请求实体
     * @return
     */
    @Override
    public BaseResponse xenditPay(XenditDTO xenditDTO) {
        int num = channelsOrderMapper.selectCountById(xenditDTO.getXenditPayRequestDTO().getExternal_id());
        ChannelsOrder co;
        if (num > 0) {
            co = channelsOrderMapper.selectByPrimaryKey(xenditDTO.getXenditPayRequestDTO().getExternal_id());
        } else {
            co = new ChannelsOrder();
        }
        co.setInstitutionOrderId(xenditDTO.getInstitutionOrderId());
        co.setTradeCurrency(xenditDTO.getTradeCurrency());
        co.setTradeAmount(xenditDTO.getXenditPayRequestDTO().getAmount());
        co.setReqIp(xenditDTO.getReqIp());
        co.setDraweeEmail(xenditDTO.getXenditPayRequestDTO().getPayer_email());
        co.setTradeStatus(TradeConstant.TRADE_WAIT);
        co.setIssuerId(xenditDTO.getXenditPayRequestDTO().getPayment_methods()[0]);
        co.setMd5KeyStr(xenditDTO.getMd5KeyStr());
        co.setId(xenditDTO.getXenditPayRequestDTO().getExternal_id());
        co.setOrderType(AD3Constant.TRADE_ORDER.toString());
        if (num > 0) {
            co.setUpdateTime(new Date());
            channelsOrderMapper.updateByPrimaryKeySelective(co);
        } else {
            co.setCreateTime(new Date());
            channelsOrderMapper.insert(co);
        }

        log.info("----------------- xendit收单接口----------------- xenditDTO:{}", JSON.toJSONString(xenditDTO));
        BaseResponse baseResponse = new BaseResponse();
        Map map = new HashMap();
        map.put("Authorization", "Basic " + xenditDTO.getMd5KeyStr());
        HttpResponse httpResponse = HttpClientUtils.reqPost(channelsConfig.getXenditCusPayUrl(), xenditDTO.getXenditPayRequestDTO(), map);
        log.info("----------------- xendit收单接口返回----------------- httpResponse:{}", JSON.toJSONString(httpResponse));
        if (httpResponse.getHttpStatus() == 200) {
            XenditPayResDTO xenditPayResDTO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), XenditPayResDTO.class);
            baseResponse.setData(xenditPayResDTO);
        } else {
            String error_code = httpResponse.getJsonObject().getString("error_code");
            baseResponse.setCode(error_code);
            baseResponse.setMsg(httpResponse.getJsonObject().getString("message"));
        }

        //String error_code = httpResponse.getJsonObject().getString("error_code");
        //if (StringUtils.isEmpty(error_code)) {
        //    XenditResDTO xenditResDTO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), XenditResDTO.class);
        //    log.info("----------------- xendit收单接口返回----------------- xenditDTO:{}", JSON.toJSONString(httpResponse));
        //    baseResponse.setData(xenditResDTO);
        //} else {
        //    baseResponse.setCode(error_code);
        //    baseResponse.setMsg(httpResponse.getJsonObject().getString("message").toString());
        //}
        return baseResponse;
    }


    /**
     * 创建一个虚拟账户
     *
     * @param bankCode
     * @param apiKey
     * @return
     */
    @Override
    public BaseResponse creatVirtualAccounts(String bankCode, String apiKey, String bankName) {
        log.info("==========================Xendit创建一个虚拟账户========================== 请求参数记录 apiKey:{}", apiKey);
        BaseResponse baseResponse = new BaseResponse();
        Map<String, Object> params = new HashMap();
        Map<String, Object> headMap = new HashMap();
        params.put("external_id", bankCode);
        params.put("bank_code", bankCode);
        params.put("name", bankName);
        headMap.put("Authorization", "Basic " + apiKey);
        HttpResponse rep = HttpClientUtils.reqPost("https://api.xendit.co/callback_virtual_accounts", params, headMap);
        log.info("==========================Xendit 创建一个虚拟账户 返回========================== rep:{}", rep);
        baseResponse.setData(rep);
        return baseResponse;
    }

    /**
     * Xendit可用银行查询接口
     *
     * @param apiKey xendit可用银行查询接口
     * @return
     */
    @Override
    public BaseResponse xenditBanks(String apiKey) {
        log.info("==========================Xendit可用银行查询接口========================== 请求参数记录 apiKey:{}", apiKey);
        BaseResponse baseResponse = new BaseResponse();
        Map<String, Object> headerMap = new HashMap<>();//请求头map
        headerMap.put("Authorization", "Basic " + apiKey);
        String str = HttpClientUtils.reqGetString(channelsConfig.getXenditBanksUrl(), null, headerMap);
        log.info("==========================Xendit可用银行查询接口========================== JSON化后的可用银行信息 str:{}", str);
        JSONArray jsonArray = JSONArray.parseArray(str);
        baseResponse.setData(jsonArray);
        return baseResponse;

    }

    /**
     * xendit根据OrderId查询订单信息
     * @param orderId
     * @param  apiKey
     * @return
     */
    @Override
    public BaseResponse getPayInfo(String orderId, String apiKey) {
        log.info("----------------- xendit 查询订单信息 ----------------- orderId:{},apiKey:{}", orderId, apiKey);
        BaseResponse baseResponse = new BaseResponse();
        Map<String, Object> params = new HashMap();
        Map<String, Object> headMap = new HashMap();
        params.put("external_id", orderId);
        headMap.put("Authorization", "Basic " + apiKey);
        String rep = HttpClientUtils.reqGetString(channelsConfig.getXenditPayUrl(), params, headMap);
        log.info("----------------- xendit 查询订单信息 ----------------- rep:{}", rep);
        if (!rep.contains("error_code")) {
            JSONArray jsonArray = JSONArray.parseArray(rep);
            List<XenditResDTO> xenditResDTO = JSON.parseArray(rep, XenditResDTO.class);
            baseResponse.setData(xenditResDTO);
        } else {
            JSONObject retJson = JSONObject.parseObject(rep);
            baseResponse.setCode(retJson.getString("error_code"));
            baseResponse.setMsg(retJson.getString("message"));
        }
        return baseResponse;
    }

}
