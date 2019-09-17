package com.payment.trade.dto;

import com.payment.common.response.HttpResponse;
import com.payment.common.utils.*;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author XuWenQi
 * @Date: 2019/7/09 13:55
 * @Description: AW线下交易测试DTO
 */
@Data
@ApiModel(value = "AW线下交易测试DTO", description = "AW线下交易测试DTO")
public class AwOfflineDTO {

    private String institutionId;
    private String operatorId;
    private BigDecimal orderAmount;
    private String orderCurrency;
    private String orderNo;
    private String orderTime;
    private int productCode;
    private String sign;
    private String terminalId;
    private String authCode;
    private String serverUrl;
    private String token;


    /**
     * 生成aw线下交易签名
     *
     * @param obj AD3公共参数输入实体
     * @return AD3回调签名
     */
    private static String createAwOfflineSign(Object obj, String md5Key) {
        //获得对象属性名对应的属性值Map
        Map<String, Object> objMap = ReflexClazzUtils.getFieldNames(obj);
        HashMap<String, String> paramMap = new HashMap<>();
        //转换成String
        for (String str : objMap.keySet()) {
            paramMap.put(str, String.valueOf(objMap.get(str)));
        }
        //排序,去空,将属性值按属性名首字母升序排序
        String signature = SignTools.getSignStr(paramMap);
        return MD5Util.getMD5String(signature + md5Key);
    }

    /**
     * 生成aw线下交易签名
     *
     * @param obj AD3公共参数输入实体
     * @return AD3回调签名
     */
    private static String createAD3OfflineSign(Object obj, String token) {
        //获得对象属性名对应的属性值Map
        Map<String, Object> objMap = ReflexClazzUtils.getFieldNames(obj);
        HashMap<String, String> paramMap = new HashMap<>();
        //转换成String
        for (String str : objMap.keySet()) {
            paramMap.put(str, String.valueOf(objMap.get(str)));
        }
        //排序,去空,将属性值按属性名首字母升序排序
        String signature = SignTools.getSignStr(paramMap);
        return MD5Util.getMD5String(signature + "&" + token);
    }


    public static void main(String[] args) {
        //线下CSB测试demo
        String nativeEnvironment = "http://localhost:9004/trade/csbScan";//本地环境
        String developerEnvironment = "http://192.168.124.7:9004/trade/csbScan";//开发环境
        String testEnvironment = "https://test.payment.com/tra/trade/csbScan";//测试环境
        String productEnvironment = "https://pag.payment.com/tra/trade/csbScan";//生产环境

        //线下CSB测试demo
        //本地环境
 /*       String md5Key = "2e9b783016e346b7b8cf05dd868706c3";
        AwOfflineDTO awOfflineDTO = new AwOfflineDTO();
        awOfflineDTO.setInstitutionId("908133567981");
        awOfflineDTO.setOperatorId("00");
        awOfflineDTO.setOrderAmount(new BigDecimal(1).setScale(2, BigDecimal.ROUND_DOWN));
        awOfflineDTO.setOrderCurrency("KRW");
        awOfflineDTO.setOrderNo(IDS.uniqueID().toString());
        awOfflineDTO.setOrderTime(DateToolUtils.formatDate(new Date()));
        awOfflineDTO.setProductCode(38);
        awOfflineDTO.setTerminalId("865150031436550");
        awOfflineDTO.setServerUrl("www.baidu.com");
        awOfflineDTO.setToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5MDgxMzM1Njc5ODEwMCIsImF1ZGllbmNlIjoid2ViIiwiY3JlYXRlZCI6MTU2NTA3NTMzMjQ0NiwiZXhwIjoxNTY1MTYxNzMyfQ.Ezx2d95pCcrq2SWJyCTPLpfw0ObdXsLmAdLvEt-uLnwxAXzU9_bYo_tkKnEqu_6rHJZuX88W4tRRxeqj8xa5cg");
        String sign = createAwOfflineSign(awOfflineDTO, md5Key);
        awOfflineDTO.setSign(sign);
        HttpResponse httpResponse = HttpClientUtils.reqPost(nativeEnvironment, awOfflineDTO, null);
        System.out.println(httpResponse);*/

        //测试环境
/*      String md5Key = "2e9b783016e346b7b8cf05dd868706c3";
        AwOfflineDTO awOfflineDTO = new AwOfflineDTO();
        awOfflineDTO.setInstitutionId("908133567981");
        awOfflineDTO.setOperatorId("00");
        awOfflineDTO.setOrderAmount(new BigDecimal(1).setScale(2, BigDecimal.ROUND_DOWN));
        awOfflineDTO.setOrderCurrency("SGD");
        awOfflineDTO.setOrderNo(IDS.uniqueID().toString());
        awOfflineDTO.setOrderTime("2019-07-09 12:00:00");
        awOfflineDTO.setProductCode(23);
        awOfflineDTO.setTerminalId("865150031436550");
        awOfflineDTO.setToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5MDgxMzM1Njc5ODEwMCIsImF1ZGllbmNlIjoid2ViIiwiY3JlYXRlZCI6MTU2NDY0MzE0Mjc1MSwiZXhwIjoxNTY0NzI5NTQyfQ.ZV4UEklsLU83WdnS_RF5ZnunU9hsUqwfC4LQ1ulCPv1tyGwH_J2o_Ti9Tr2h6rcurdKht5oId2VUJLAnRUoN0Q");
        awOfflineDTO.setServerUrl("www.baidu.com");
        String sign = createAwOfflineSign(awOfflineDTO, md5Key);
        awOfflineDTO.setSign(sign);
        HttpResponse httpResponse = HttpClientUtils.reqPost(testEnvironment, awOfflineDTO, null);
        System.out.println(httpResponse);*/

        //生产环境
        String md5Key = "2e9b783016e346b7b8cf05dd868706c3";
        //测试token
        //String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5MDgxMzM1Njc5ODEwMCIsImF1ZGllbmNlIjoid2ViIiwiY3JlYXRlZCI6MTU2NDY0MzE0Mjc1MSwiZXhwIjoxNTY0NzI5NTQyfQ.ZV4UEklsLU83WdnS_RF5ZnunU9hsUqwfC4LQ1ulCPv1tyGwH_J2o_Ti9Tr2h6rcurdKht5oId2VUJLAnRUoN0Q";
        //本地token
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5MDgxMzM1Njc5ODEwMCIsImF1ZGllbmNlIjoid2ViIiwiY3JlYXRlZCI6MTU2NTA3NTMzMjQ0NiwiZXhwIjoxNTY1MTYxNzMyfQ.Ezx2d95pCcrq2SWJyCTPLpfw0ObdXsLmAdLvEt-uLnwxAXzU9_bYo_tkKnEqu_6rHJZuX88W4tRRxeqj8xa5cg";
        AwOfflineDTO awOfflineDTO = new AwOfflineDTO();
        awOfflineDTO.setInstitutionId("908133567981");
        awOfflineDTO.setOperatorId("00");
        awOfflineDTO.setOrderAmount(new BigDecimal(1000).setScale(2, BigDecimal.ROUND_DOWN));
        awOfflineDTO.setOrderCurrency("SGD");
        awOfflineDTO.setOrderNo(IDS.uniqueID().toString());
        awOfflineDTO.setOrderTime(DateToolUtils.formatDate(new Date()));
        awOfflineDTO.setProductCode(1);
        awOfflineDTO.setTerminalId("865067036012493");
        //    eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5MDgxMzM1Njc5ODEwMCIsImF1ZGllbmNlIjoid2ViIiwiY3JlYXRlZCI6MTU2NTA3NTMzMjQ0NiwiZXhwIjoxNTY1MTYxNzMyfQ.Ezx2d95pCcrq2SWJyCTPLpfw0ObdXsLmAdLvEt-uLnwxAXzU9_bYo_tkKnEqu_6rHJZuX88W4tRRxeqj8xa5cg
        //测试 eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5MDgxMzM1Njc5ODEwMCIsImF1ZGllbmNlIjoid2ViIiwiY3JlYXRlZCI6MTU2NDY0MzE0Mjc1MSwiZXhwIjoxNTY0NzI5NTQyfQ.ZV4UEklsLU83WdnS_RF5ZnunU9hsUqwfC4LQ1ulCPv1tyGwH_J2o_Ti9Tr2h6rcurdKht5oId2VUJLAnRUoN0Q
        awOfflineDTO.setToken(token);
        //awOfflineDTO.setServerUrl("https://test.payment.com/tra/trade/testCallback");
        awOfflineDTO.setServerUrl("http://www.baidu.com");
        String sign = createAwOfflineSign(awOfflineDTO, md5Key);
        awOfflineDTO.setSign(sign);
        HttpResponse httpResponse = HttpClientUtils.reqPost(nativeEnvironment, awOfflineDTO, null);
        System.out.println(httpResponse);

        String bscNativeEnvironment = "http://localhost:9004/trade/bscScan";//本地环境
        String bscDeveloperEnvironment = "http://192.168.124.7:9004/trade/bscScan";//开发环境
        String bscTestEnvironment = "https://test.payment.com/tra/trade/bscScan";//测试环境
        String bscProductEnvironment = "https://pag.payment.com/tra/trade/bscScan";//生产环境

        //线下BSC测试demo
        //本地环境
    /*  String md5Key = "2e9b783016e346b7b8cf05dd868706c3";
        AwOfflineDTO awOfflineDTO = new AwOfflineDTO();
        awOfflineDTO.setInstitutionId("908133567981");
        awOfflineDTO.setOperatorId("00");
        awOfflineDTO.setOrderAmount(new BigDecimal(0.01).setScale(2, BigDecimal.ROUND_DOWN));
        awOfflineDTO.setOrderCurrency("SGD");
        awOfflineDTO.setOrderNo("413");
        awOfflineDTO.setOrderTime("2019-07-09 16:11:00");
        awOfflineDTO.setProductCode(12);
        awOfflineDTO.setTerminalId("865150031436550");
        awOfflineDTO.setAuthCode("134513080664919131");
        awOfflineDTO.setServerUrl("www.baidu.com");
        awOfflineDTO.setToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5MDgxMzM1Njc5ODEwMCIsImF1ZGllbmNlIjoid2ViIiwiY3JlYXRlZCI6MTU2NDA0NTE4ODYwMywiZXhwIjoxNTY0MTMxNTg4fQ.LbW7bNn-Zyn_sKXqNtrVPja7gFgEFgD7cb5LeA-vHKy5IsfHfq-ho34FvQMFtym-AF8trtnQ0PsxzOaFOBtEzA");
        String sign = createAwOfflineSign(awOfflineDTO, md5Key);
        awOfflineDTO.setSign(sign);
        HttpResponse httpResponse = HttpClientUtils.reqPost(bscNativeEnvironment, awOfflineDTO, null);
        System.out.println(httpResponse);*/

        //测试环境
/*      String md5Key = "633d776b82f04502a7949cac64c19d41";
        AwOfflineDTO awOfflineDTO = new AwOfflineDTO();
        awOfflineDTO.setInstitutionId("890020459383");
        awOfflineDTO.setOperatorId("00");
        awOfflineDTO.setOrderAmount(new BigDecimal(0.01).setScale(2, BigDecimal.ROUND_DOWN));
        awOfflineDTO.setOrderCurrency("SGD");
        awOfflineDTO.setOrderNo("521522");
        awOfflineDTO.setOrderTime("2019-07-09 12:00:00");
        awOfflineDTO.setProductCode(23);
        awOfflineDTO.setTerminalId("861741040109230");
        awOfflineDTO.setServerUrl("https://test.payment.com/tra/trade/testCallback");
        awOfflineDTO.setToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5MDgxMzM1Njc5ODEwMCIsImF1ZGllbmNlIjoid2ViIiwiY3JlYXRlZCI6MTU2NDY0MzE0Mjc1MSwiZXhwIjoxNTY0NzI5NTQyfQ.ZV4UEklsLU83WdnS_RF5ZnunU9hsUqwfC4LQ1ulCPv1tyGwH_J2o_Ti9Tr2h6rcurdKht5oId2VUJLAnRUoN0Q");
        String sign = createAwOfflineSign(awOfflineDTO, md5Key);
        awOfflineDTO.setSign(sign);
        HttpResponse httpResponse = HttpClientUtils.reqPost(testEnvironment, awOfflineDTO, null);
        System.out.println(httpResponse);*/

        //生产环境
  /*    String md5Key = "2e9b783016e346b7b8cf05dd868706c3";
        AwOfflineDTO awOfflineDTO = new AwOfflineDTO();
        awOfflineDTO.setInstitutionId("908133567981");
        awOfflineDTO.setOperatorId("00");
        awOfflineDTO.setOrderAmount(new BigDecimal(0.01).setScale(2, BigDecimal.ROUND_DOWN));
        awOfflineDTO.setOrderCurrency("SGD");
        awOfflineDTO.setOrderNo("215");
        awOfflineDTO.setOrderTime("2019-07-09 12:00:00");
        awOfflineDTO.setProductCode(19);
        awOfflineDTO.setTerminalId("865150031436550");
        awOfflineDTO.setServerUrl("www.baidu.com");
        awOfflineDTO.setToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5MDgxMzM1Njc5ODEwMCIsImF1ZGllbmNlIjoid2ViIiwiY3JlYXRlZCI6MTU2MzI1MTQwNjMzOSwiZXhwIjoxNTYzMzM3ODA2fQ.HHnDCxDUdLJ54O7YTrrQFaCWPxiiOh37mKKFhXXXoYRhiUVaEjoTHrUvvqsajali7GrhAhgzUKx7tlQqZ5EzLA");
        String sign = createAwOfflineSign(awOfflineDTO, md5Key);
        awOfflineDTO.setSign(sign);
        HttpResponse httpResponse = HttpClientUtils.reqPost(bscProductEnvironment, awOfflineDTO, null);
        System.out.println(httpResponse);*/

        /*String token = "e6af1fe792b492923155adb18523bc96";
        AD3OfflineCallbackDTO ad3OfflineCallbackDTO = new AD3OfflineCallbackDTO();
        ad3OfflineCallbackDTO.setVersion("v1.0");
        ad3OfflineCallbackDTO.setInputCharset("1");
        ad3OfflineCallbackDTO.setLanguage("1");
        ad3OfflineCallbackDTO.setMerchantId("20190524500137");
        ad3OfflineCallbackDTO.setOperatorId("00");
        ad3OfflineCallbackDTO.setMerorderNo("O933510252249976832");
        ad3OfflineCallbackDTO.setMerorderDatetime("20190827145453");
        ad3OfflineCallbackDTO.setMerorderCurrency("SGD");
        ad3OfflineCallbackDTO.setMerorderAmount("0.01");
        ad3OfflineCallbackDTO.setPayAmount("0.01");
        ad3OfflineCallbackDTO.setPayerName("E2RGMrYX");
        ad3OfflineCallbackDTO.setBusinessType("2");
        ad3OfflineCallbackDTO.setPayType("71");
        ad3OfflineCallbackDTO.setIssuerId("alipay");
        ad3OfflineCallbackDTO.setReceiveUrl("https://test.payment.com/tra/offlineCallback/ad3Callback");
        ad3OfflineCallbackDTO.setBody("商品");
        ad3OfflineCallbackDTO.setTxnId("PPO20190827504509");
        ad3OfflineCallbackDTO.setTxnDate("20190827145454");
        ad3OfflineCallbackDTO.setStatus("3");
        ad3OfflineCallbackDTO.setRespcode("10000");
        ad3OfflineCallbackDTO.setRespmsg("SUCCESS");
        //ad3OfflineCallbackDTO.setSignMsg("41BDBDB2A2730AF98F6D58F6AD9A945A");
        //ad3OfflineCallbackDTO.setSignMsg(null);
        String newSignMsg = createAD3OfflineSign(ad3OfflineCallbackDTO, token);//生成ad3回调接口签名
        System.out.println(newSignMsg);*/
    }
}
