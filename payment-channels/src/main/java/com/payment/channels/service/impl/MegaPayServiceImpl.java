package com.payment.channels.service.impl;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.payment.channels.config.ChannelsConfig;
import com.payment.channels.dao.ChannelsOrderMapper;
import com.payment.channels.service.MegaPayService;
import com.payment.common.constant.AD3Constant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.megapay.*;
import com.payment.common.entity.ChannelsOrder;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.BeanToMapUtil;
import com.payment.common.utils.MD5;
import com.payment.common.utils.XMLUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-05-30 15:09
 **/
@Service
@Slf4j
public class MegaPayServiceImpl implements MegaPayService {

    @Autowired
    private ChannelsConfig channelsConfig;

    @Autowired
    private ChannelsOrderMapper channelsOrderMapper;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/5/28
     * @Descripate megaPayTHB收单接口
     **/
    @Override
    public BaseResponse megaPayTHB(MegaPayRequestDTO megaPayRequestDTO) {
        int num = channelsOrderMapper.selectCountById(megaPayRequestDTO.getOrderID());
        ChannelsOrder co;
        if (num > 0) {
            co = channelsOrderMapper.selectByPrimaryKey(megaPayRequestDTO.getOrderID());
        } else {
            co = new ChannelsOrder();
        }
        co.setInstitutionOrderId(megaPayRequestDTO.getInstitutionOrderId());
        co.setTradeCurrency(megaPayRequestDTO.getTradeCurrency());
        co.setTradeAmount(new BigDecimal(megaPayRequestDTO.getAmt()));
        co.setReqIp(megaPayRequestDTO.getReqIp());
        co.setDraweeName(megaPayRequestDTO.getC_Name());
        co.setBrowserUrl(megaPayRequestDTO.getRetURL());
        co.setTradeStatus(TradeConstant.TRADE_WAIT);
        co.setIssuerId(megaPayRequestDTO.getBMode());
        co.setMd5KeyStr(megaPayRequestDTO.getMd5KeyStr());
        co.setId(megaPayRequestDTO.getOrderID());
        co.setOrderType(AD3Constant.TRADE_ORDER.toString());
        if (num > 0) {
            co.setUpdateTime(new Date());
            channelsOrderMapper.updateByPrimaryKeySelective(co);
        } else {
            co.setCreateTime(new Date());
            channelsOrderMapper.insert(co);
        }
        BaseResponse response = new BaseResponse();
        log.info("-----------------megaPayTHB收单接口----------------- megaPayRequestDTO:{}", JSON.toJSONString(megaPayRequestDTO));
        long start = System.currentTimeMillis();
        cn.hutool.http.HttpResponse execute = HttpRequest.post(channelsConfig.getMegaPayTHBUrl())
                .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .form(BeanToMapUtil.beanToMap(megaPayRequestDTO))
                .timeout(20000)
                .execute();
        long end = System.currentTimeMillis();
        log.info("-------megaPayTHB通道消耗时间-------Time:{} MS", (end - start));
        int status = execute.getStatus();
        //判断HTTP状态码
//        if (status != AsianWalletConstant.HTTP_SUCCESS_STATUS) {
//            log.info("----------------------向上游接口发送订单失败日志记录----------------------http状态码:{},megaPayRequestDTO:{}", status, JSON.toJSON(megaPayRequestDTO));
//            response.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
//            return response;
//        }
        String body = execute.body();
        log.info("----------------------megaPayTHB返回body----------------------body:{}", body);
        if (StringUtils.isEmpty(body)) {
            log.info("----------------------向上游接口发送订单失败日志记录----------------------megaPayRequestDTO:{}", JSON.toJSON(megaPayRequestDTO));
            response.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return response;
        }
        response.setData(body);
        return response;

    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/5/28
     * @Descripate megaPayIDR收单接口
     **/
    @Override
    public BaseResponse megaPayIDR(MegaPayIDRRequestDTO megaPayIDRRequestDTO) {
        int num = channelsOrderMapper.selectCountById(megaPayIDRRequestDTO.getE_inv());
        ChannelsOrder co;
        if (num > 0) {
            co = channelsOrderMapper.selectByPrimaryKey(megaPayIDRRequestDTO.getE_inv());
        } else {
            co = new ChannelsOrder();
        }
        co.setInstitutionOrderId(megaPayIDRRequestDTO.getInstitutionOrderId());
        co.setTradeCurrency(megaPayIDRRequestDTO.getTradeCurrency());
        co.setTradeAmount(new BigDecimal(megaPayIDRRequestDTO.getE_amt()));
        co.setReqIp(megaPayIDRRequestDTO.getReqIp());
        co.setDraweeName(megaPayIDRRequestDTO.getCusName());
        co.setBrowserUrl(megaPayIDRRequestDTO.getE_respURL());
        co.setTradeStatus(TradeConstant.TRADE_WAIT);
        co.setIssuerId(megaPayIDRRequestDTO.getBMode());
        co.setMd5KeyStr(megaPayIDRRequestDTO.getMd5KeyStr());
        co.setId(megaPayIDRRequestDTO.getE_inv());
        co.setOrderType(AD3Constant.TRADE_ORDER.toString());
        if (num > 0) {
            co.setUpdateTime(new Date());
            channelsOrderMapper.updateByPrimaryKeySelective(co);
        } else {
            co.setCreateTime(new Date());
            channelsOrderMapper.insert(co);
        }

        BaseResponse response = new BaseResponse();
        log.info("-----------------megaPayIDR收单接口----------------- megaPayIDRRequestDTO:{}", JSON.toJSONString(megaPayIDRRequestDTO));
        long start = System.currentTimeMillis();
        cn.hutool.http.HttpResponse execute = HttpRequest.post(channelsConfig.getMegaPayIDRUrl())
                .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .form(BeanToMapUtil.beanToMap(megaPayIDRRequestDTO))
                .timeout(20000)
                .execute();
        long end = System.currentTimeMillis();
        log.info("-------megaPayIDR通道消耗时间-------Time:{} MS", (end - start));
        int status = execute.getStatus();
        //判断HTTP状态码
        if (status != AsianWalletConstant.HTTP_SUCCESS_STATUS) {
            log.info("----------------------向上游接口发送订单失败日志记录----------------------http状态码:{},megaPayIDRRequestDTO:{}", status, JSON.toJSON(megaPayIDRRequestDTO));
            response.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return response;
        }
        String body = execute.body();
        log.info("----------------------megaPayIDR返回body----------------------body:{}", body);
        if (StringUtils.isEmpty(body)) {
            log.info("----------------------向上游接口发送订单失败日志记录----------------------megaPayIDRRequestDTO:{}", JSON.toJSON(megaPayIDRRequestDTO));
            response.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return response;
        }
        response.setData(body);
        return response;

    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/5/28
     * @Descripate nextPos收单接口
     **/
    @Override
    public BaseResponse nextPos(NextPosRequestDTO nextPosRequestDTO) throws Exception {
        log.info("-----------------nextPos收单接口-----------------【请求参数】 nextPosRequestDTO:{}", JSON.toJSONString(nextPosRequestDTO));
        int num = channelsOrderMapper.selectCountById(nextPosRequestDTO.getEinv());
        ChannelsOrder co;
        if (num > 0) {
            co = channelsOrderMapper.selectByPrimaryKey(nextPosRequestDTO.getEinv());
        } else {
            co = new ChannelsOrder();
        }
        co.setInstitutionOrderId(nextPosRequestDTO.getInstitutionOrderId());
        co.setTradeCurrency("SGD");
        co.setTradeAmount(new BigDecimal(nextPosRequestDTO.getAmt()));
        co.setReqIp(nextPosRequestDTO.getReqIp());
        co.setServerUrl(nextPosRequestDTO.getReturn_url());
        co.setTradeStatus(TradeConstant.TRADE_WAIT);
        co.setId(nextPosRequestDTO.getEinv());
        co.setOrderType(AD3Constant.TRADE_ORDER.toString());
        if (num > 0) {
            co.setUpdateTime(new Date());
            channelsOrderMapper.updateByPrimaryKeySelective(co);
        } else {
            co.setCreateTime(new Date());
            channelsOrderMapper.insert(co);
        }
        BaseResponse baseResponse = new BaseResponse();
        PostMethod post = new PostMethod(channelsConfig.getNextPosUrl());
        HttpClient httpclient = new HttpClient();
        NameValuePair[] param = {
                new NameValuePair("merID", nextPosRequestDTO.getMerID()),//商户号
                new NameValuePair("einv", nextPosRequestDTO.getEinv()),//订单号
                new NameValuePair("amt", nextPosRequestDTO.getAmt()),//金额
                //new NameValuePair("c_Email", dmsg.get("15916210566@163.com")),//顾客邮箱，非必填
                new NameValuePair("product", nextPosRequestDTO.getProduct()),//产品名
                new NameValuePair("return_url", nextPosRequestDTO.getReturn_url())//接受异步通知的URL
        };
        post.setRequestBody(param);
        // 设置编码
        httpclient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");

        int stats = 0;
        stats = httpclient.executeMethod(post);
        String respstr = new String(post.getResponseBody(), "UTF-8");
        //logger.info("NextPost支付申请“生成二维码qrUrl”返回信息==================>" + respstr);
        log.info("-----------------nextPos收单接口返回----------------- stats:{}，respstr:{}", stats, respstr);
        if (stats == 200) {
            // 注解方式xml转换为对象
            Map<String, Object> respXml2Map = XMLUtil.xml2Map(respstr);
            Map<String, String> map1 = new HashMap<String, String>();//装回签处理后数据
            if (!respXml2Map.containsKey("errMessage")) {//有任何错误就会返回异常信息errMessage，和errDec ；；不会返回其他字段
                String qrString = respXml2Map.get("qrString").toString();//二维码字符串
                //获取不到二维码字符串的场合
                if (StringUtils.isEmpty(qrString)) {
                    baseResponse.setCode(TradeConstant.HTTP_FAIL);
                    baseResponse.setMsg(TradeConstant.HTTP_FAIL_MSG);
                    return baseResponse;
                }
                String mark = respXml2Map.get("mark").toString();//md5签名
                log.info("-----------------nextPos收单接口返回-----------------respXml2Map:{}", respXml2Map.toString());
                Base64 b64 = new Base64();
                String aaa = b64.encodeToString(qrString.getBytes());
                String generateContextByResultData = MD5.MD5Encode(aaa + nextPosRequestDTO.getMerRespPassword() + nextPosRequestDTO.getMerRespID());
                generateContextByResultData = generateContextByResultData.toUpperCase();
                //判断回签结果并处理
                if (generateContextByResultData.equals(mark)) {
                    //把二维码链接发给前台
                    baseResponse.setCode(String.valueOf(stats));
                    baseResponse.setMsg("success");
                    baseResponse.setData(qrString);
                } else {
                    //验证签名不通过
                    baseResponse.setCode(String.valueOf(302));
                    log.info("验证签名不通过,NextPos,onlineCSB:NextPosSign=" + mark);
                    log.info("验证签名不通过:自己：sign=" + generateContextByResultData);
                    baseResponse.setMsg("验证签名不通过");
                    baseResponse.setData(null);
                }
            } else {
                baseResponse.setCode(String.valueOf(302));
                log.info("-----------------nextPos请求失败----------------- respXml2Map:{}", JSON.toJSONString(respXml2Map));
                baseResponse.setMsg("fail");
                baseResponse.setCode(String.valueOf(stats));
            }
        } else {
            baseResponse.setCode(String.valueOf(302));
            log.info("-----------------nextPos请求失败----------------- stats:{}", JSON.toJSONString(stats));
            baseResponse.setCode(String.valueOf(stats));
            baseResponse.setMsg("fail");
        }
        return baseResponse;
    }

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/8/9
     * @Descripate nextPos查询接口
     **/
    @Override
    public BaseResponse nextPosQuery(NextPosQueryDTO nextPosQueryDTO) {
        log.info("==================【NextPos查询订单】==================【请求参数】 nextPosQueryDTO: {}", JSON.toJSONString(nextPosQueryDTO));
        String nextPosQueryUrl = channelsConfig.getNextPosQueryUrl();
        log.info("==================【NextPos查询订单】==================【查询请求URL】 nextPosQueryUrl: {}", nextPosQueryUrl);
        BaseResponse baseResponse = new BaseResponse();
        try {
            cn.hutool.http.HttpResponse execute = HttpRequest.post(nextPosQueryUrl)
                    .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .form(BeanToMapUtil.beanToMap(nextPosQueryDTO))
                    .timeout(10000)
                    .execute();
            int status = execute.getStatus();
            if (status != 200) {
                log.info("==================【NextPos查询订单】==================【状态码异常】 status: {}", status);
                baseResponse.setMsg(TradeConstant.HTTP_FAIL_MSG);
                baseResponse.setCode(TradeConstant.HTTP_FAIL);
                return baseResponse;
            }
            String body = execute.body();
            log.info("==================【NextPos查询订单】==================【xml响应参数】 body: {}", body);
            //响应Map
            Map<String, Object> respMap = XMLUtil.xml2Map(body);
            log.info("==================【NextPos查询订单】==================【解析后的响应参数】 respMap: {}", JSON.toJSONString(respMap));
            String mark = String.valueOf(respMap.get("mark"));
            //金额格式转换
            DecimalFormat df = new DecimalFormat("#,##0.00");
            BigDecimal bigDecimal = new BigDecimal(String.valueOf(respMap.get("amt")));
            String formatAmt = df.format(bigDecimal);
            byte[] refCodes = cn.hutool.core.codec.Base64.decode(respMap.get("refCode").toString());
            String clearText = new String(refCodes) + respMap.get("einv") + nextPosQueryDTO.getMerRespPassword() +
                    nextPosQueryDTO.getMerRespID() + respMap.get(nextPosQueryDTO.getMerRespID()) + formatAmt;
            log.info("==================【NextPos查询订单】==================【通道响应结果】签名前的明文 clearText: {}", clearText);
            String mySign = MD5.MD5Encode(clearText).toUpperCase();
            log.info("==================【NextPos查询订单】==================【通道响应结果】签名后的密文 mySign: {}", mySign);
            if (!mark.equals(mySign)) {
                log.info("==================【NextPos查询订单】==================【签名不匹配】");
                baseResponse.setCode(TradeConstant.HTTP_SUCCESS);
                baseResponse.setMsg(TradeConstant.HTTP_FAIL_MSG);
                return baseResponse;
            }
            //有任何错误就会返回异常信息errMessage,和errDec,不会返回其他字段
            if (respMap.containsKey("errMessage")) {
                log.info("==================【NextPos查询订单】==================【通道响应报文有错误信息】 errMessage: {}", respMap.get("errMessage"));
                baseResponse.setCode(TradeConstant.HTTP_SUCCESS);
                baseResponse.setMsg(String.valueOf(respMap.get("errMessage")));
                return baseResponse;
            }
            //查询成功
            log.info("==================【NextPos查询订单】==================【查询成功】");
            baseResponse.setCode(TradeConstant.HTTP_SUCCESS);
            baseResponse.setMsg(TradeConstant.HTTP_SUCCESS_MSG);
            baseResponse.setData(respMap);
        } catch (Exception e) {
            log.info("==================【NextPos查询订单】==================【接口异常】", e);
            baseResponse.setMsg(TradeConstant.HTTP_FAIL_MSG);
            baseResponse.setCode(TradeConstant.HTTP_FAIL);
        }
        return baseResponse;
    }

    /**
     * NextPos退款接口
     *
     * @param nextPosRefundDTO nextPos退款实体
     * @return BaseResponse
     */
    @Override
    public BaseResponse nextPosRefund(NextPosRefundDTO nextPosRefundDTO) {
        //组装签名前的明文
        String requestClearText = nextPosRefundDTO.getTradeNo() + nextPosRefundDTO.getOrderId() + nextPosRefundDTO.getMerRespPassword() +
                nextPosRefundDTO.getMerRespID() + nextPosRefundDTO.getAmt();
        log.info("==================【NextPos退款】==================【请求通道前的明文】 requestClearText: {}", requestClearText);
        String requestSign = MD5.MD5Encode(requestClearText).toUpperCase();
        nextPosRefundDTO.setMark(requestSign);
        log.info("==================【NextPos退款】==================【请求参数】 nextPosRefundDTO: {}", JSON.toJSONString(nextPosRefundDTO));
        String nextPosRefundUrl = channelsConfig.getNextPosRefundUrl();
        log.info("==================【NextPos退款】==================【退款请求URL】 nextPosRefundUrl: {}", nextPosRefundUrl);
        BaseResponse baseResponse = new BaseResponse();
        try {
            Map<String, Object> refundMap = new HashMap<>();
            refundMap.put("merID", nextPosRefundDTO.getMerID());
            refundMap.put("orderID", nextPosRefundDTO.getOrderId());
            refundMap.put("refundType", nextPosRefundDTO.getRefundType());
            refundMap.put("originalAmt", nextPosRefundDTO.getOriginalAmt());
            refundMap.put("amt", nextPosRefundDTO.getAmt());
            refundMap.put("tradeNo", nextPosRefundDTO.getTradeNo());
            refundMap.put("mark", nextPosRefundDTO.getMark());
            log.info("==================【NextPos退款】==================【NextPos退款】请求参数 refundMap: {}", JSON.toJSONString(refundMap));
            cn.hutool.http.HttpResponse execute = HttpRequest.post(nextPosRefundUrl)
                    .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .form(refundMap)
                    .timeout(30000)
                    .execute();
            int status = execute.getStatus();
            if (status != 200) {
                log.info("==================【NextPos退款】==================【状态码异常】 status: {}", status);
                log.info("==================【NextPos退款】==================【响应参数】 body: {}", execute.body());
                baseResponse.setMsg(TradeConstant.HTTP_FAIL_MSG);
                baseResponse.setCode(TradeConstant.HTTP_FAIL);
                return baseResponse;
            }
            String body = execute.body();
            log.info("==================【NextPos退款】==================【xml响应参数】 body: {}", body);
            //解析xml参数
            Map<String, Object> respMap = XMLUtil.xml2Map(body);
            log.info("==================【NextPos退款】==================【解析后的响应参数】 respMap: {}", JSON.toJSONString(respMap));
            //金额格式转换
            DecimalFormat df = new DecimalFormat("#,##0.00");
            BigDecimal bigDecimal = new BigDecimal(String.valueOf(respMap.get("amt")));
            String formatAmt = df.format(bigDecimal);
            byte[] refCodes = cn.hutool.core.codec.Base64.decode(String.valueOf(respMap.get("refCode")));
            //签名前的明文
            String clearText = new String(refCodes) + respMap.get("einv") + nextPosRefundDTO.getMerRespPassword() +
                    nextPosRefundDTO.getMerRespID() + respMap.get(nextPosRefundDTO.getMerRespID()) + formatAmt;
            log.info("==================【NextPos退款】==================【通道响应结果】签名前的明文 clearText: {}", clearText);
            String mySign = MD5.MD5Encode(clearText).toUpperCase();
            log.info("==================【NextPos退款】==================【通道响应结果】签名后的密文 mySign: {}", mySign);
            //校验通道响应签名
            if (!String.valueOf(respMap.get("mark")).equals(mySign)) {
                log.info("==================【NextPos退款】==================【响应签名不匹配】");
                baseResponse.setCode(TradeConstant.HTTP_SUCCESS);
                baseResponse.setMsg(TradeConstant.HTTP_FAIL_MSG);
                return baseResponse;
            }
            //有任何错误就会返回异常信息errMessage,和errDec,不会返回其他字段
            if (respMap.containsKey("errMessage")) {
                log.info("==================【NextPos退款】==================【通道响应报文有错误信息】 errMessage: {}", respMap.get("errMessage"));
                baseResponse.setCode(TradeConstant.HTTP_SUCCESS);
                baseResponse.setMsg(String.valueOf(respMap.get("errMessage")));
                return baseResponse;
            }
            String refundStatus = String.valueOf(respMap.get(nextPosRefundDTO.getMerRespID()));
            if (!StringUtils.isEmpty(refundStatus) && "000".equals(refundStatus)) {
                log.info("==================【NextPos退款】==================【退款成功】");
                baseResponse.setCode(TradeConstant.HTTP_SUCCESS);
                baseResponse.setMsg(TradeConstant.HTTP_SUCCESS_MSG);
                baseResponse.setData(respMap);
            } else {
                baseResponse.setCode(TradeConstant.HTTP_SUCCESS);
                baseResponse.setMsg(TradeConstant.HTTP_FAIL_MSG);
            }
        } catch (Exception e) {
            log.error("==================【NextPos退款】==================【接口异常】", e);
            baseResponse.setMsg(TradeConstant.HTTP_FAIL_MSG);
            baseResponse.setCode(TradeConstant.HTTP_FAIL);
        }
        return baseResponse;
    }
}
