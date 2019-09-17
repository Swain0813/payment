package com.payment.trade.controller;
import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import com.payment.trade.dto.AD3OfflineCallbackDTO;
import com.payment.trade.dto.EnetsPosCallbackDTO;
import com.payment.trade.service.OfflineCallbackService;
import com.payment.trade.channels.enets.EnetsService;
import com.payment.trade.channels.wechat.WechatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author XuWenQi
 * @Date: 2019/3/28 13:02
 * @Description: 线下回调接口
 */
@RestController
@Api(description = "线下回调接口")
@RequestMapping("/offlineCallback")
@Slf4j
public class OfflineCallbackController extends BaseController {

    @Autowired
    private OfflineCallbackService offlineCallbackService;

    @Autowired
    private WechatService wechatService;

    @Autowired
    private EnetsService enetsService;

    @ApiOperation(value = "ad3线下回调接口")
    @PostMapping("/ad3Callback")
    public String ad3Callback(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap == null || parameterMap.size() == 0) {
            log.info("=================【AD3线下回调接口信息记录】=================【回调参数记录为空】");
            throw new BusinessException(EResultEnum.CALLBACK_PARAMETER_IS_NULL.getCode());
        }
        log.info("=================【AD3线下回调接口信息记录】=================【回调参数记录】 parameterMap:{}", JSON.toJSONString(parameterMap));
        HashMap<String, String> dtoMap = new HashMap<>();
        Set<String> set = parameterMap.keySet();
        for (String key : set) {
            dtoMap.put(key, parameterMap.get(key)[0]);
        }
        AD3OfflineCallbackDTO ad3OfflineCallbackDTO = JSON.parseObject(JSON.toJSONString(dtoMap), AD3OfflineCallbackDTO.class);
        log.info("=================【AD3线下回调接口信息记录】=================【JSON解析后的回调参数记录】 ad3OfflineCallbackDTO:{}", JSON.toJSONString(ad3OfflineCallbackDTO));
        return offlineCallbackService.ad3Callback(ad3OfflineCallbackDTO);
    }

    @ApiOperation(value = "eNetsPOS扫码服务器回调")
    @PostMapping("/eNetsPosCSBCallback")
    public ResponseEntity<Void> eNetsPosCSBCallback(HttpServletRequest request, HttpServletResponse response) {
        //用流的方式接收数据
        StringBuffer jsonResMsg = new StringBuffer();
        String line = null;
        BufferedReader reader = null;
        try {
            reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jsonResMsg.append(line);
            }
        } catch (IOException e) {
            log.error("------------------eNetsPOS扫码服务器回调接口信息记录----------------读取回调参数IO异常", e);
        }
        String strResMsg = jsonResMsg.toString();
        log.info("------------------eNetsPOS扫码服务器回调接口信息记录----------------接受到的回调参数记录 strResMsg:{}", strResMsg);
        JSONObject apiResMsgObj = JSONObject.fromObject(strResMsg);
        String stan = apiResMsgObj.getString("stan");
        String retrieval_ref = apiResMsgObj.getString("retrieval_ref");
        String txn_identifier = apiResMsgObj.getString("txn_identifier");//结算币种
        String response_code = apiResMsgObj.getString("response_code");
        EnetsPosCallbackDTO enetsPosCallbackDTO = new EnetsPosCallbackDTO(stan, retrieval_ref, txn_identifier, response_code);
        log.info("--------------------------------eNetsPOS扫码服务器回调接口信息记录--------------------------------eNetsPOS扫码服务器回调接口信息记录回调参数记录 enetsPosCallbackDTO:{}", JSON.toJSON(enetsPosCallbackDTO));
        return enetsService.eNetsPosCSBCallback(enetsPosCallbackDTO, response);
    }

    @ApiOperation(value = "wechat线下CSB回调")
    @PostMapping("/wechatCSBCallback")
    public void wechatCSBCallback(HttpServletRequest request, HttpServletResponse response) {
        wechatService.wechatCSBCallback(request, response);
    }
}
