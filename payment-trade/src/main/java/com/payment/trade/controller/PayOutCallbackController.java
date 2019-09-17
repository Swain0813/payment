package com.payment.trade.controller;
import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.trade.dto.Help2PayOutCallbackDTO;
import com.payment.trade.service.PayOutCallBackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 付款回调服务
 */
@Slf4j
@RestController
@Api(description = "付款回调服务")
@RequestMapping("/payOutCallBack")
public class PayOutCallbackController extends BaseController {
    @Autowired
    private PayOutCallBackService payOutCallBackService;

    @ApiOperation(value = "help2Pay付款回调接口")
    @PostMapping("/help2PayCallBack")
    public void help2PayCallBack(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size() == 0) {
            log.info("------------------help2PayCallBack---------------- 回调参数为空");
            return;
        }
        log.info("--------------------------------help2PayCallBack回调服务器接口信息记录--------------------------------参数记录 parameterMap:{}", JSON.toJSON(parameterMap));
        HashMap<String, String> dtoMap = new HashMap<>();
        Set<String> set = parameterMap.keySet();
        for (String key : set) {
            dtoMap.put(key, parameterMap.get(key)[0]);
        }
        Help2PayOutCallbackDTO help2PayOutCallbackDTO = JSON.parseObject(JSON.toJSONString(dtoMap), Help2PayOutCallbackDTO.class);
        log.info("-------------------------------help2PayCallBack回调服务器接口信息记录--------------------------------JSON解析后的参数记录 help2PayOutCallbackDTO:{}", JSON.toJSON(help2PayOutCallbackDTO));
        payOutCallBackService.help2PayCallBack(help2PayOutCallbackDTO);
    }


}
