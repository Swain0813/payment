package com.payment.trade.utils;

/**
 * 多线程测试并发
 */
public class TestConcurrent {

    // 总的请求个数
    private static final int requestTotal = 1000;

    // 同一时刻最大的并发线程的个数
    private static final int concurrentThreadNum = 500;

    //线程单独变量
    private static ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

//    public static void main(String[] args) throws InterruptedException {
//        //ExecutorService executorService = Executors.newCachedThreadPool();
//        ExecutorService executorService = Executors.newFixedThreadPool(concurrentThreadNum);
//        CountDownLatch countDownLatch = new CountDownLatch(requestTotal);
//        //Semaphore semaphore = new Semaphore(concurrentThreadNum);
//        for (int i = 0; i < requestTotal; i++) {
//           /* executorService.execute(() -> {
//                try {
//                    //semaphore.acquire();
//                    String result = testRequestUri();
//                    System.out.println("result:" + result);
//                    // semaphore.release();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                //countDownLatch.countDown();
//            });*/
//            executorService.execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        String result = testCSBScan(IDS.uniqueID().toString());
//                        System.out.println("result:" + result);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    //计数器减1
//                    countDownLatch.countDown();
//                }
//            });
//        }
//        countDownLatch.await();//计数器等待
//        executorService.shutdown();//释放线程池
//        System.out.println("请求完成");
//        testCSBScan("66323236");
//    }
//
//    /**
//     * 线下CSB接口
//     *
//     * @param institutionOrderId 机构订单号
//     * @return 响应结果
//     */
//    private static String testCSBScan(String institutionOrderId) {
//        PlaceOrdersDTO p = new PlaceOrdersDTO();
//        //必填参数
//        p.setInstitutionOrderId(institutionOrderId);
//        p.setInstitutionCode("890020459383");
//        p.setOrderCurrency("USD");
//        p.setInstitutionOrderTime("2019-05-14 10:00:00");
//        p.setAmount(new BigDecimal(1));
//        p.setProductCode(32);
//        p.setDeviceCode("865067036012493");
//        p.setDeviceOperator("00");
//        p.setToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI4OTAwMjA0NTkzODMwMCIsImF1ZGllbmNlIjoid2ViIiwiY3JlYXRlZCI6MTU1NjU4OTgyNjgwOCwiZXhwIjoxNTU2Njc2MjI2fQ.T0kF01B0uOIjE3OAluBX5Y-tPeTOSQ1--smtFjG3a-8z7SBuDkz-ryWAyBkFBysK35_82hpXNy55a4z6UtycPA");
//        //非必填参数
//        p.setInstitutionName("测试");
//        p.setSecondInstitutionName("测试");
//        p.setSecondInstitutionCode("测试");
//        p.setCommodityName("测试");
//        p.setGoodsDescription("测试");
//        p.setDraweeName("测试");
//        p.setDraweeAccount("测试");
//        p.setDraweeBank("测试");
//        p.setDraweeEmail("测试");
//        p.setDraweePhone("12232323232");
//        p.setRemark1("测试备注1");
//        p.setRemark2("测试备注2");
//        p.setRemark3("测试备注3");
//        //签名
//        p.setSign(createSign(p));
//        HttpResponse httpResponse = HttpClientUtils.reqPost("http://192.168.124.31:9004/trade/csbScan", p, null);
//        return String.valueOf(httpResponse.getJsonObject());
//    }
//
//    /**
//     * 生成线下接口签名
//     *
//     * @param obj
//     * @return
//     */
//    public static String createSign(Object obj) {
//        HashMap<String, String> paramMap = new HashMap<>();
//        Map<String, Object> proValMap = ReflexClazzUtils.getFieldNames(obj);
//        for (String pro : proValMap.keySet()) {
//            paramMap.put(pro, String.valueOf(proValMap.get(pro)));
//        }
//        String sortSign = SignTools.getSignStr(paramMap);//密文字符串拼装处理
//        String sign = MD5Util.getMD5String(sortSign);
//        return sign;
//    }
}
