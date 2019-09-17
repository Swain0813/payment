package com.payment.task.service.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.HttpResponse;
import com.payment.common.utils.HttpClientUtils;
import com.payment.common.utils.MD5Util;
import com.payment.common.utils.ReflexClazzUtils;
import com.payment.common.utils.SignTools;
import com.payment.common.vo.FundChangeVO;
import com.payment.task.dao.ClearingMapper;
import com.payment.task.service.ClearingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * 清结算接口实现类
 */
@Slf4j
@Service
public class ClearingServiceImpl implements ClearingService {

    @Autowired
    private ClearingMapper clearingMapper;

    /**
     * 资金变动接口
     * 场景支付成功后上报清结算系统
     *
     * @return
     */
    @Override
    public BaseResponse fundChange(FundChangeDTO fundChangeDTO, Map<String, Object> headerMap) {
        log.info("------------ task项目 上报清结算 请求 fundChange ------------ fundChangeDTO : {}; headerMap : {} ", JSON.toJSON(fundChangeDTO), JSON.toJSON(headerMap));
        BaseResponse baseResponse = new BaseResponse();
        fundChangeDTO.setSignMsg(createSignature(fundChangeDTO));
        String url = clearingMapper.selectUrlByKey(AsianWalletConstant.CSAPI_URL_INTOACCOUNTJSON);
        HttpResponse httpResponse = HttpClientUtils.reqPost(url, fundChangeDTO, headerMap);
        //比如亚洲钱包清结算系统没有启动或者没有响应的场合
        if (httpResponse.getHttpStatus() == null || httpResponse.getHttpStatus() != AsianWalletConstant.HTTP_SUCCESS_STATUS) {
            throw new BusinessException(EResultEnum.REPORT_QJS_ERROR.getCode());
        }
        FundChangeVO fundChangeVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), FundChangeVO.class);
        baseResponse.setData(fundChangeVO);
        baseResponse.setMsg(fundChangeVO.getRespMsg());
        baseResponse.setCode(httpResponse.getHttpStatus().toString());
        log.info("------------ task项目 上报清结算 返回 fundChange ------------ BaseResponse : {}", JSON.toJSON(baseResponse));
        return baseResponse;
    }

    /**
     * 生成清结算接口签名
     * @param obj 接口参数对象
     * @return 签名
     */
    public String createSignature(Object obj) {
        Map<String, Object> commonMap = ReflexClazzUtils.getFieldNames(obj);
        HashMap<String, String> paramMap = new HashMap<>();
        if (!StringUtils.isEmpty(commonMap.get("reconciliationId"))) {
            commonMap.put("reconciliationId", null);
        }
        for (String str : commonMap.keySet()) {
            paramMap.put(str, String.valueOf(commonMap.get(str)));
        }
        String str = SignTools.getSignStr(paramMap);//密文字符串拼装处理
        String msg = clearingMapper.selectUrlByKey(AsianWalletConstant.CSAPI_MD5KEY) + str;
        log.info("-----------------task项目 清结算上报签名原文-----------------msg:{}", msg);
        String signature = MD5Util.getMD5String(msg).toLowerCase();
        log.info("************task项目 生成清结算接口签名: **************",signature);
        return signature;
    }
}
