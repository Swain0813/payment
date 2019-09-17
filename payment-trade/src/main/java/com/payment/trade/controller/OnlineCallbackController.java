package com.payment.trade.controller;
import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.TradeConstant;
import com.payment.common.entity.Channel;
import com.payment.common.entity.Orders;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.trade.channels.alipay.AliPayService;
import com.payment.trade.channels.cfp.CloudFlashPayService;
import com.payment.trade.channels.eghl.EGHLService;
import com.payment.trade.channels.enets.EnetsService;
import com.payment.trade.channels.help2pay.Help2PayService;
import com.payment.trade.channels.megaPay.MegaPayService;
import com.payment.trade.channels.vtc.VTCService;
import com.payment.trade.channels.xendit.XenditService;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dto.*;
import com.payment.trade.service.CommonService;
import com.payment.trade.service.OnlineCallbackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author shenxinran
 * @Date: 2019/3/15 13:53
 * @Description: 线上网关回调接口
 */
@RestController
@Api(description = "线上网关回调接口")
@RequestMapping("/onlinecallback")
@Slf4j
public class OnlineCallbackController extends BaseController {

    @Autowired
    private OnlineCallbackService onlineCallbackService;

    @Autowired
    private EGHLService eghlService;

    @Autowired
    private MegaPayService megaPayService;

    @Autowired
    private VTCService vtcService;

    @Autowired
    private EnetsService enetsService;

    @Autowired
    private Help2PayService help2PayService;

    @Autowired
    private XenditService xenditService;

    @Autowired
    private AliPayService aliPayService;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CloudFlashPayService cloudFlashPayService;

    @ApiOperation(value = "线上AD3回调接口")
    @PostMapping("/callback")
    public String callback(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size() == 0) {
            log.info("------------------线上网关回调接口参数为空----------------");
            throw new BusinessException(EResultEnum.CALLBACK_PARAMETER_IS_NULL.getCode());
        }
        HashMap<String, String> dtoMap = new HashMap<>();
        Set<String> set = parameterMap.keySet();
        for (String key : set) {
            dtoMap.put(key, parameterMap.get(key)[0]);
        }
        AD3OnlineCallbackDTO ad3OnlineCallbackDTO = JSON.parseObject(JSON.toJSONString(dtoMap), AD3OnlineCallbackDTO.class);
        log.info("--------------------------------线上回调信息记录--------------------------------AD3OnlineCallbackDTO:{}", JSON.toJSON(ad3OnlineCallbackDTO));
        return onlineCallbackService.callback(ad3OnlineCallbackDTO);
    }

    @ApiOperation(value = "AD3线上浏览器地址回调处理方法")
    @PostMapping("/paysuccess")
    @CrossOrigin
    public void paysuccess(HttpServletRequest request, HttpServletResponse response) {
        //AD3 支付宝与微信无浏览器回调
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size() == 0) {
            log.info("------------------线上网关pickupUrl参数为空----------------");
            throw new BusinessException(EResultEnum.CALLBACK_PARAMETER_IS_NULL.getCode());
        }
        HashMap<String, String> dtoMap = new HashMap<>();
        Set<String> set = parameterMap.keySet();
        for (String key : set) {
            dtoMap.put(key, parameterMap.get(key)[0]);
        }
        AD3OnlineCallbackDTO aavo = JSON.parseObject(JSON.toJSONString(dtoMap), AD3OnlineCallbackDTO.class);
        onlineCallbackService.jump(aavo, response);
    }

    @ApiOperation(value = "EGHL回调服务器")
    @PostMapping("/eghlServerCallback")
    public void eghlServerCallback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size() == 0) {
            log.info("------------------EGHL回调服务器参数为空----------------");
            return;
        }
        log.info("--------------------------------EGHL回调服务器接口信息记录--------------------------------参数记录 parameterMap:{}", JSON.toJSON(parameterMap));
        HashMap<String, String> dtoMap = new HashMap<>();
        Set<String> set = parameterMap.keySet();
        for (String key : set) {
            dtoMap.put(key, parameterMap.get(key)[0]);
        }
        EghlBrowserCallbackDTO eghlBrowserCallbackDTO = JSON.parseObject(JSON.toJSONString(dtoMap), EghlBrowserCallbackDTO.class);
        log.info("--------------------------------EGHL回调服务器接口信息记录--------------------------------JSON解析后的参数记录 eghlBrowserCallbackDTO:{}", JSON.toJSON(eghlBrowserCallbackDTO));
        eghlService.eghlServerCallback(eghlBrowserCallbackDTO, response);
    }

    @ApiOperation(value = "EGHL回调浏览器")
    @PostMapping("/eghlBrowserCallback")
    public void eghlBrowserCallback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size() == 0) {
            log.info("------------------EGHL回调浏览器接口信息记录----------------回调参数为空");
            return;
        }
        log.info("--------------------------------EGHL回调浏览器接口信息记录--------------------------------参数记录 parameterMap:{}", JSON.toJSON(parameterMap));
        HashMap<String, String> dtoMap = new HashMap<>();
        Set<String> set = parameterMap.keySet();
        for (String key : set) {
            dtoMap.put(key, parameterMap.get(key)[0]);
        }
        EghlBrowserCallbackDTO eghlBrowserCallbackDTO = JSON.parseObject(JSON.toJSONString(dtoMap), EghlBrowserCallbackDTO.class);
        log.info("--------------------------------EGHL回调浏览器接口信息记录--------------------------------EGHL回调浏览器回调参数记录 eghlBrowserCallbackDTO:{}", JSON.toJSON(eghlBrowserCallbackDTO));
        eghlService.eghlBrowserCallback(eghlBrowserCallbackDTO, response);
    }

    @ApiOperation(value = "megaPayTHB服务器回调")
    @PostMapping("/megaPayThbServerCallback")
    public void megaPayThbServerCallback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size() == 0) {
            log.info("------------------megaPayTHB服务器回调接口信息记录----------------回调参数为空");
            return;
        }
        HashMap<String, String> dtoMap = new HashMap<>();
        Set<String> set = parameterMap.keySet();
        for (String key : set) {
            dtoMap.put(key, parameterMap.get(key)[0]);
        }
        log.info("--------------------------------megaPayTHB服务器回调接口信息记录--------------------------------megaPayTHB服务器回调接口回调参数记录 dtoMap:{}", JSON.toJSON(dtoMap));
        MegaPayServerCallbackDTO megaPayServerCallbackDTO = JSON.parseObject(JSON.toJSONString(dtoMap), MegaPayServerCallbackDTO.class);
        log.info("--------------------------------megaPayTHB服务器回调接口信息记录--------------------------------JSON解析后的回调参数记录 megaPayServerCallbackDTO:{}", JSON.toJSON(megaPayServerCallbackDTO));
        megaPayService.megaPayThbServerCallback(megaPayServerCallbackDTO, request, response);
    }

    @ApiOperation(value = "megaPayTHB浏览器回调")
    @PostMapping("/megaPayThbBrowserCallback")
    public void megaPayThbBrowserCallback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size() == 0) {
            log.info("------------------megaPayTHB浏览器回调接口信息记录----------------回调参数为空");
            return;
        }
        HashMap<String, String> dtoMap = new HashMap<>();
        Set<String> set = parameterMap.keySet();
        for (String key : set) {
            dtoMap.put(key, parameterMap.get(key)[0]);
        }
        log.info("--------------------------------megaPayTHB浏览器回调接口信息记录--------------------------------megaPayTHB浏览器回调接口回调参数记录 dtoMap:{}", JSON.toJSON(dtoMap));
        MegaPayBrowserCallbackDTO megaPayBrowserCallbackDTO = JSON.parseObject(JSON.toJSONString(dtoMap), MegaPayBrowserCallbackDTO.class);
        log.info("--------------------------------megaPayTHB浏览器回调接口信息记录--------------------------------json解析后的回调参数记录 megaPayBrowserCallbackDTO:{}", JSON.toJSON(megaPayBrowserCallbackDTO));
        megaPayService.megaPayThbBrowserCallback(megaPayBrowserCallbackDTO, response);
    }

    @ApiOperation(value = "megaPayIDR服务器回调")
    @PostMapping("/megaPayIdrServerCallback")
    public void megaPayIdrServerCallback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size() == 0) {
            log.info("------------------megaPayIDR服务器回调接口信息记录----------------回调参数为空");
            return;
        }
        HashMap<String, String> dtoMap = new HashMap<>();
        Set<String> set = parameterMap.keySet();
        for (String key : set) {
            dtoMap.put(key, parameterMap.get(key)[0]);
        }
        log.info("--------------------------------megaPayIDR服务器回调接口信息记录--------------------------------megaPayIDR服务器回调接口回调参数记录 dtoMap:{}", JSON.toJSON(dtoMap));
        MegaPayIDRServerCallbackDTO megaPayIDRServerCallbackDTO = JSON.parseObject(JSON.toJSONString(dtoMap), MegaPayIDRServerCallbackDTO.class);
        log.info("--------------------------------megaPayIDR服务器回调接口信息记录--------------------------------json解析后的回调参数记录 dtoMap:{}", JSON.toJSON(megaPayIDRServerCallbackDTO));
        megaPayService.megaPayIdrServerCallback(megaPayIDRServerCallbackDTO, request, response);
    }

    @ApiOperation(value = "megaPayIDR浏览器回调")
    @PostMapping("/megaPayIdrBrowserCallback")
    public void megaPayIdrBrowserCallback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size() == 0) {
            log.info("------------------megaPayIDR浏览器回调接口信息记录----------------回调参数为空");
            return;
        }
        HashMap<String, String> dtoMap = new HashMap<>();
        Set<String> set = parameterMap.keySet();
        for (String key : set) {
            dtoMap.put(key, parameterMap.get(key)[0]);
        }
        log.info("--------------------------------megaPayIDR浏览器回调接口信息记录--------------------------------megaPayIDR浏览器回调接口回调参数记录 dtoMap:{}", JSON.toJSON(dtoMap));
        MegaPayIDRBrowserCallbackDTO megaPayIDRBrowserCallbackDTO = JSON.parseObject(JSON.toJSONString(dtoMap), MegaPayIDRBrowserCallbackDTO.class);
        log.info("--------------------------------megaPayIDR浏览器回调接口信息记录--------------------------------json解析后回调参数记录 megaPayIDRBrowserCallbackDTO:{}", JSON.toJSON(megaPayIDRBrowserCallbackDTO));
        megaPayService.megaPayIdrBrowserCallback(megaPayIDRBrowserCallbackDTO, response);
    }

    @ApiOperation(value = "NextPos回调")
    @PostMapping("/nextPosCallback")
    public void nextPosCallback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> respMap = new HashMap<>();
        Map requestParams = request.getParameterMap();
        log.info("================【NextPos回调】================【回调参数记录】 requestParams:{}", JSON.toJSON(requestParams));
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            respMap.put(name, valueStr);
        }
        if (respMap.get("einv").startsWith("PGO")) {
            Map<String, Object> ad3Map = new HashMap<>();
            Set<String> set = respMap.keySet();
            for (String key : set) {
                ad3Map.put(key, respMap.get(key));
            }
            log.info("================【NextPos回调】================【该笔回调订单属于AD3】 orderId:{}", respMap.get("einv"));
            String body = commonService.nextPosCallbackAD3(ad3Map);
            try {
                response.getWriter().write(body);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        String einv = respMap.get("einv");
        Orders orders = ordersMapper.selectByPrimaryKey(einv);
        if (orders == null) {
            log.info("================【NextPos回调】================【回调订单信息不存在】");
            return;
        }
        //校验订单状态
        if (!TradeConstant.ORDER_PAYING.equals(orders.getTradeStatus())) {
            log.info("=================【NextPos回调】=================【订单状态不为支付中】");
            try {
                response.getWriter().write("00");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        Channel channel = commonService.getChannelByChannelCode(orders.getChannelCode());
        String status = respMap.get(channel.getPayCode());//H875247
        String refCode = respMap.get("refCode");
        String amt = respMap.get("amt");
        String transactionID = respMap.get("transactionID");
        String mark = respMap.get("mark");
        NextPosCallbackDTO nextPosCallbackDTO = new NextPosCallbackDTO();
        nextPosCallbackDTO.setEinv(einv);//订单id
        nextPosCallbackDTO.setRefCode(refCode);//响应码
        nextPosCallbackDTO.setAmt(amt);//金额
        nextPosCallbackDTO.setTransactionID(transactionID);//通道流水号
        nextPosCallbackDTO.setMark(mark);//签名
        nextPosCallbackDTO.setStatus(status);//订单状态
        nextPosCallbackDTO.setMerRespID(channel.getPayCode());
        nextPosCallbackDTO.setMerRespPassword(channel.getMd5KeyStr());
        log.info("================【NextPos回调】================【JSON格式化后的回调参数记录】 nextPosCallbackDTO:{}", JSON.toJSONString(nextPosCallbackDTO));
        megaPayService.nextPosCallback(nextPosCallbackDTO, orders, response);
    }

    @ApiOperation(value = "vtc服务器回调")
    @PostMapping("/vtcServerCallback")
    public void vtcServerCallback(HttpServletRequest request, HttpServletResponse response) {
        //获取vtc的通知返回参数
        String data = request.getParameter("data");
        String signature = request.getParameter("signature");
        //解析data数据
        String[] strArr = data.split("\\|");
        String amount = strArr[0];
        String message = strArr[1];
        String paymentType = strArr[2];
        String reference_number = strArr[3];
        String status = strArr[4];
        String trans_ref_no = strArr[5];
        String website_id = strArr[6];
        VtcCallbackDTO vtcCallbackDTO = new VtcCallbackDTO();
        vtcCallbackDTO.setAmount(amount);
        vtcCallbackDTO.setMessage(message);
        vtcCallbackDTO.setPayment_type(paymentType);
        vtcCallbackDTO.setReference_number(reference_number);
        vtcCallbackDTO.setStatus(status);
        vtcCallbackDTO.setTrans_ref_no(trans_ref_no);
        vtcCallbackDTO.setWebsite_id(website_id);
        vtcCallbackDTO.setSignature(signature);
        log.info("--------------------------------vtc服务器回调接口信息记录--------------------------------vtc服务器回调接口回调参数记录 vtcCallbackDTO:{}", JSON.toJSON(vtcCallbackDTO));
        vtcService.vtcPayServerCallback(vtcCallbackDTO, data, response);
    }

    @ApiOperation(value = "vtc浏览器回调")
    @PostMapping("/vtcPayBrowserCallback")
    public void vtcPayBrowserCallback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size() == 0) {
            log.info("------------------vtc浏览器回调接口信息记录----------------回调参数为空");
            return;
        }
        HashMap<String, String> dtoMap = new HashMap<>();
        Set<String> set = parameterMap.keySet();
        for (String key : set) {
            dtoMap.put(key, parameterMap.get(key)[0]);
        }
        VtcCallbackDTO vtcCallbackDTO = JSON.parseObject(JSON.toJSONString(dtoMap), VtcCallbackDTO.class);
        log.info("--------------------------------vtc浏览器回调接口信息记录--------------------------------vtc浏览器回调接口回调参数记录 vtcCallbackDTO:{}", JSON.toJSON(vtcCallbackDTO));
        vtcService.vtcPayBrowserCallback(vtcCallbackDTO, response);
    }

    @ApiOperation(value = "enets网银浏览器回调")
    @PostMapping("/eNetsBankBrowserCallback")
    public void eNetsBankBrowserCallback(HttpServletRequest request, HttpServletResponse response) {
        //页面返回处理
        Object message = request.getParameter("message");//contains TxnRes message
        String txnRes = String.valueOf(message);
        try {
            txnRes = URLDecoder.decode(txnRes + "", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("--------------------------------enets网银浏览器回调发生异常--------------------------------", e);
        }
        log.info("--------------------------------enets网银浏览器回调接口信息记录--------------------------------enets网银浏览器回调接口回调参数记录 txnRes:{}", txnRes);
        EnetsCallbackDTO enetsCallbackDTO = null;
        //返回结果数据结构不一致的特殊处理
        if (txnRes.contains("ss")) {
            EnetsOutCallbackDTO enetsOutCallbackDTO = JSON.parseObject(txnRes, EnetsOutCallbackDTO.class);
            enetsCallbackDTO = enetsOutCallbackDTO.getMsg();
        } else {
            enetsCallbackDTO = JSON.parseObject(txnRes, EnetsCallbackDTO.class);
        }
        if (enetsCallbackDTO == null) {
            log.info("--------------------------------enets网银浏览器回调接口信息记录--------------------------------回调参数为空");
            return;
        }
        //获取签名
        String hmac = request.getParameter("hmac");
        enetsCallbackDTO.setHmac(hmac);//签名
        log.info("--------------------------------enets网银浏览器回调接口信息记录--------------------------------enets网银浏览器回调接口回调参数记录 enetsCallbackDTO:{}", JSON.toJSON(enetsCallbackDTO));
        enetsService.eNetsBankBrowserCallback(enetsCallbackDTO, txnRes, response);
    }

    @ApiOperation(value = "enets网银服务器回调")
    @PostMapping(value = "/eNetsBankServerCallback", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> eNetsBankServerCallback(@RequestBody String txnRes, HttpServletRequest request, HttpServletResponse response) {
        JSONObject json = JSONObject.fromObject(txnRes);
        String msg = json.getString("msg");
        EnetsCallbackDTO enetsCallbackDTO = JSON.parseObject(msg, EnetsCallbackDTO.class);
        String hmac = request.getHeader("hmac");
        enetsCallbackDTO.setHmac(hmac);//签名
        log.info("--------------------------------enets网银服务器回调接口信息记录--------------------------------enets网银服务器回调接口回调参数记录 enetsCallbackDTO:{}", JSON.toJSON(enetsCallbackDTO));
        if (enetsCallbackDTO == null) {
            log.info("--------------------------------enets网银服务器回调接口信息记录--------------------------------回调参数为空");
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        return enetsService.eNetsBankServerCallback(enetsCallbackDTO, txnRes, response);
    }

    @ApiOperation(value = "enets线上扫码服务器回调")
    @PostMapping(value = "/eNetsQrCodeServerCallback")
    public ResponseEntity<Void> eNetsQrCodeServerCallback(HttpServletRequest request, HttpServletResponse response) {
        StringBuffer jsonResMsg = null;
        BufferedReader reader = null;
        //用流的方式接收数据
        try {
            jsonResMsg = new StringBuffer();
            String line = null;
            reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jsonResMsg.append(line);
            }
        } catch (Exception e) {
            log.info("------------------enets线上扫码服务器回调接口信息记录----------------读取流数据发生错误", e);
        }
        String strResMsg = jsonResMsg.toString();
        log.info("------------------enets线上扫码服务器回调接口信息记录----------------回调参数记录 strResMsg:{}", strResMsg);
        JSONObject apiResMsgObj = JSONObject.fromObject(strResMsg);
        JSONObject msg = apiResMsgObj.getJSONObject("msg");
        EnetsCallbackDTO enetsCallbackDTO = JSON.parseObject(String.valueOf(msg), EnetsCallbackDTO.class);
        String hmac = request.getHeader("hmac");
        log.info("------------------enets线上扫码服务器回调接口信息记录----------------回调参数记录 签名:{}", hmac);
        enetsCallbackDTO.setHmac(hmac);//签名
        log.info("--------------------------------enets线上扫码服务器回调接口信息记录--------------------------------json化后的回调参数记录 enetsCallbackDTO:{}", JSON.toJSON(enetsCallbackDTO));
        if (enetsCallbackDTO == null) {
            log.info("--------------------------------enets线上扫码服务器回调接口信息记录--------------------------------json化后的回调参数为空");
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("--------------------------------enets线上扫码服务器回调发生异常--------------------------------", e);
            }
        }
        return enetsService.eNetsQrCodeServerCallback(enetsCallbackDTO, strResMsg, response);
    }


    @ApiOperation(value = "enets线上扫码浏览器回调")
    @PostMapping("/eNetsQrCodeBrowserCallback")
    public void eNetsQrCodeBrowserCallback(HttpServletRequest request, HttpServletResponse response) {
        //页面返回处理
        Object message = request.getParameter("message");//contains TxnRes message
        String txnRes = String.valueOf(message);
        try {
            txnRes = URLDecoder.decode(txnRes + "", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("-----------------------enets线上扫码浏览器回调发生异常-----------------------", e);
        }
        log.info("--------------------------------enets线上扫码浏览器回调接口信息记录--------------------------------回调参数记录 txnRes:{}", txnRes);
        EnetsCallbackDTO enetsCallbackDTO = null;
        //返回结果数据结构不一致的特殊处理
        if (txnRes.contains("ss")) {
            EnetsOutCallbackDTO enetsOutCallbackDTO = JSON.parseObject(txnRes, EnetsOutCallbackDTO.class);
            enetsCallbackDTO = enetsOutCallbackDTO.getMsg();
        } else {
            enetsCallbackDTO = JSON.parseObject(txnRes, EnetsCallbackDTO.class);
        }
        if (enetsCallbackDTO == null) {
            log.info("--------------------------------enets线上扫码浏览器回调接口信息记录--------------------------------回调参数为空");
            return;
        }
        //获取签名
        String hmac = request.getParameter("hmac");
        enetsCallbackDTO.setHmac(hmac);//签名
        log.info("--------------------------------enets线上扫码浏览器回调接口信息记录--------------------------------json化后的回调参数记录 enetsCallbackDTO:{}", JSON.toJSON(enetsCallbackDTO));
        enetsService.eNetsQrCodeBrowserCallback(enetsCallbackDTO, txnRes, response);
    }


    @ApiOperation(value = "help2Pay浏览器回调")
    @PostMapping("/help2PayBrowserCallback")
    public void help2PayBrowserCallback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        log.info("--------------------------------help2Pay浏览器回调接口信息记录-------------------------------回调参数记录 parameterMap:{}", JSON.toJSON(parameterMap));
        if (parameterMap.size() == 0) {
            log.info("------------------help2Pay浏览器回调接口信息记录----------------回调参数为空");
            return;
        }
        HashMap<String, String> dtoMap = new HashMap<>();
        Set<String> set = parameterMap.keySet();
        for (String key : set) {
            dtoMap.put(key, parameterMap.get(key)[0]);
        }
        Help2PayCallbackDTO help2PayCallbackDTO = JSON.parseObject(JSON.toJSONString(dtoMap), Help2PayCallbackDTO.class);
        log.info("--------------------------------help2Pay浏览器回调接口信息记录-------------------------------JSON解析后的参数记录 help2PayCallbackDTO:{}", JSON.toJSON(help2PayCallbackDTO));
        help2PayService.help2PayBrowserCallback(help2PayCallbackDTO, response);
    }

    @ApiOperation(value = "help2Pay服务器回调")
    @PostMapping("/help2PayServerCallback")
    public void help2PayServerCallback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        log.info("--------------------------------help2Pay服务器回调接口信息记录-------------------------------回调参数记录 parameterMap:{}", JSON.toJSON(parameterMap));
        if (parameterMap.size() == 0) {
            log.info("------------------help2Pay服务器回调接口信息记录----------------回调参数为空");
            return;
        }
        HashMap<String, String> dtoMap = new HashMap<>();
        Set<String> set = parameterMap.keySet();
        for (String key : set) {
            dtoMap.put(key, parameterMap.get(key)[0]);
        }
        Help2PayCallbackDTO help2PayCallbackDTO = JSON.parseObject(JSON.toJSONString(dtoMap), Help2PayCallbackDTO.class);
        log.info("--------------------------------help2Pay服务器回调接口信息记录-------------------------------JSON解析后的回调参数记录 help2PayCallbackDTO:{}", JSON.toJSON(help2PayCallbackDTO));
        help2PayService.help2PayServerCallback(help2PayCallbackDTO, response);
    }

    @ApiOperation(value = "xendit服务器回调")
    @RequestMapping("/xenditServerCallback")
    public void xenditServerCallback(@RequestBody XenditServerCallbackDTO xenditServerCallbackDTO) {
        log.info("--------------------------------xendit服务器回调接口信息记录-------------------------------回调参数记录 xenditServerCallbackDTO:{}", JSON.toJSON(xenditServerCallbackDTO));
        xenditService.xenditServerCallback(xenditServerCallbackDTO);
    }

    @ApiOperation(value = "aliPay支付CSB扫码服务器回调")
    @PostMapping("/aliPayCB_TPMQRCReturn")
    public void aliPayCB_TPMQRCReturn(HttpServletRequest request, HttpServletResponse response) {
        log.info("--------------------------------aliPay支付CSB扫码服务器回调-------------------------------回调参数记录 ");
        aliPayService.aliPayCB_TPMQRCReturn(request, response);
    }

    @ApiOperation(value = "NL通道浏览器回调")
    @RequestMapping("/nLBrowserCallback")
    public void nLBrowserCallback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        log.info("------------------NL通道浏览器回调接口信息记录----------------回调参数 parameterMap:{}", JSON.toJSONString(parameterMap));
    }

    @ApiOperation(value = "NL通道服务器回调")
    @RequestMapping("/nLServerCallback")
    public void nLServerCallback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        //此处服务器回调上报清结算前需要判断订单币种是否存在于账户 ******sxr
        log.info("------------------NL通道服务器回调接口信息记录----------------回调参数 parameterMap:{}", JSON.toJSONString(parameterMap));
    }


    @ApiOperation(value = "云闪付前端回调")
    @PostMapping("/cloudFlashPayCallback")
    @CrossOrigin
    public BaseResponse cloudFlashPayCallback(@RequestBody CloudFlashCallbackDTO cloudFlashCallbackDTO) {
        log.info("===================【云闪付前端回调】===================【回调参数】 cloudFlashCallbackDTO:{}", JSON.toJSONString(cloudFlashCallbackDTO));
        return cloudFlashPayService.cloudFlashPayCallback(cloudFlashCallbackDTO);
    }
}
