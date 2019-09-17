package com.payment.permission.service.impl;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.PayOutRequestDTO;
import com.payment.common.entity.OrderRefund;
import com.payment.common.entity.Orders;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.BeanToMapUtil;
import com.payment.common.utils.ReflexClazzUtils;
import com.payment.common.vo.*;
import com.payment.permission.service.OrdersFeignService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrdersFeignServiceImpl implements OrdersFeignService {

    /**
     * Excel 导出功能
     *
     * @param orders 订单对象集合
     * @param clazz  Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getExcelWriter(List<Orders> orders, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (Orders order : orders) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(order);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("tradeType")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("收款");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("付款");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("tradeDirection")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("线上");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("线下");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("exchangeStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("换汇成功");
                            } else {
                                oList2.add("换汇失败");
                            }
                        } else if (s.equals("tradeStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("待付款");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("付款中");
                            } else if (String.valueOf(oMap.get(s)).equals("3")) {
                                oList2.add("付款成功");
                            } else if (String.valueOf(oMap.get(s)).equals("4")) {
                                oList2.add("付款失败");
                            } else if (String.valueOf(oMap.get(s)).equals("5")) {
                                oList2.add("已过期");
                            } else if (String.valueOf(oMap.get(s)).equals("6")) {
                                oList2.add("退款");
                            } else if (String.valueOf(oMap.get(s)).equals("7")) {
                                oList2.add("撤销中");
                            } else if (String.valueOf(oMap.get(s)).equals("8")) {
                                oList2.add("撤销成功");
                            } else if (String.valueOf(oMap.get(s)).equals("9")) {
                                oList2.add("撤销失败");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("rateType")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("单笔费率");
                            } else {
                                oList2.add("单笔定额");
                            }
                        } else if (s.equals("feePayer")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("内扣");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("外扣");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("chargeStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("成功");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("失败");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("clearStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("待清算");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("清算成功");
                            } else if (String.valueOf(oMap.get(s)).equals("3")) {
                                oList2.add("清算失败");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("settleStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("待结算");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("结算成功");
                            } else if (String.valueOf(oMap.get(s)).equals("3")) {
                                oList2.add("结算失败");
                            } else {
                                oList2.add("");
                            }
                        } else {
                            oList2.add(oMap.get(s));
                        }
                    }
                }
            }
            oList1.add(oList2);
        }
        oList1.add(0, oSet1);
        writer.write(oList1);
        return writer;
    }

    /**
     * Excel 导出功能
     *
     * @param list  对象集合
     * @param clazz 类名Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getRefundOrderExcelWriter(List<OrderRefund> list, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (OrderRefund orderRefund : list) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(orderRefund);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("refundStatus")) {
                            if ((oMap.get(s).toString()).equals("1")) {
                                oList2.add("待退款");
                            } else if ((oMap.get(s).toString()).equals("2")) {
                                oList2.add("退款成功");
                            } else {
                                oList2.add("退款失败");
                            }
                        } else if (s.equals("refundType")) {
                            if ((oMap.get(s).toString()).equals("1")) {
                                oList2.add("全额退款");
                            } else if ((oMap.get(s).toString()).equals("2")) {
                                oList2.add("部分退款");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("tradeDirection")) {
                            if ((oMap.get(s).toString()).equals("1")) {
                                oList2.add("线上");
                            } else if ((oMap.get(s).toString()).equals("2")) {
                                oList2.add("线下");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("refundMode")) {
                            if ((oMap.get(s).toString()).equals("1")) {
                                oList2.add("系统退款");
                            } else if ((oMap.get(s).toString()).equals("2")) {
                                oList2.add("人工退款");
                            } else {
                                oList2.add("");
                            }
                        } else {
                            oList2.add(oMap.get(s));
                        }
                    }
                }
            }
            oList1.add(oList2);
        }
        oList1.add(0, oSet1);
        writer.write(oList1);
        return writer;
    }

    /**
     * 交易一览查询导出功能
     *
     * @param orders
     * @param clazz
     * @return
     */
    @Override
    public ExcelWriter getOrderExcelWriter(List<OrderTradeVO> orders, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (OrderTradeVO orderTradeVO : orders) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(orderTradeVO);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("tradeDirection")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("线上");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("线下");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("tradeStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("待支付 ");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("交易中");
                            } else if (String.valueOf(oMap.get(s)).equals("3")) {
                                oList2.add("交易成功");
                            } else if (String.valueOf(oMap.get(s)).equals("4")) {
                                oList2.add("交易失败");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("cancelStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("撤销中 ");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("撤销成功");
                            } else if (String.valueOf(oMap.get(s)).equals("3")) {
                                oList2.add("撤销失败");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("refundStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("退款中 ");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("部分退款成功");
                            } else if (String.valueOf(oMap.get(s)).equals("3")) {
                                oList2.add("退款成功");
                            } else if (String.valueOf(oMap.get(s)).equals("4")) {
                                oList2.add("退款失败");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("tradeType")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("收");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("付");
                            } else {
                                oList2.add("");
                            }
                        } else {
                            oList2.add(oMap.get(s));
                        }
                    }
                }
            }
            oList1.add(oList2);
        }
        oList1.add(0, oSet1);
        writer.write(oList1);
        return writer;
    }

    /**
     * 机构交易一览导出功能
     *
     * @param orders
     * @param clazz
     * @return
     */
    @Override
    public ExcelWriter getInstitutionOrderExcelWriter(List<InstitutionOrderTradeVO> orders, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (InstitutionOrderTradeVO orderTradeVO : orders) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(orderTradeVO);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("tradeDirection")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("Online Trading");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("Offline Trading");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("tradeStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("Payment Pending");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("Payment Processing");
                            } else if (String.valueOf(oMap.get(s)).equals("3")) {
                                oList2.add("Payment Success");
                            } else if (String.valueOf(oMap.get(s)).equals("4")) {
                                oList2.add("Payment Fail");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("cancelStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("Reverse Pending");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("Reverse Success");
                            } else if (String.valueOf(oMap.get(s)).equals("3")) {
                                oList2.add("Reverse Fail");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("refundStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("Refund Pending");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("Part Of The Refund Success");
                            } else if (String.valueOf(oMap.get(s)).equals("3")) {
                                oList2.add("Refund Success");
                            } else if (String.valueOf(oMap.get(s)).equals("4")) {
                                oList2.add("Refund Fail");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("tradeType")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("Collection");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("Payment");
                            } else {
                                oList2.add("");
                            }
                        } else {
                            oList2.add(oMap.get(s));
                        }
                    }
                }
            }
            oList1.add(oList2);
        }
        oList1.add(0, oSet1);
        writer.write(oList1);
        return writer;
    }

    /**
     * DCC报表导出
     *
     * @param dccReportList
     * @param clazz
     * @return
     */
    @Override
    public ExcelWriter getDccReportExcelWriter(List<DccReportVO> dccReportList, Class<DccReportVO> clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (DccReportVO dccReportVO : dccReportList) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(dccReportVO);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("tradeStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("Payment Pending");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("Payment Processing");
                            } else if (String.valueOf(oMap.get(s)).equals("3")) {
                                oList2.add("Payment Success");
                            } else if (String.valueOf(oMap.get(s)).equals("4")) {
                                oList2.add("Payment Fail");
                            } else if (String.valueOf(oMap.get(s)).equals("5")) {
                                oList2.add("Have Expired");
                            } else {
                                oList2.add("");
                            }
                        } else {
                            oList2.add(oMap.get(s));
                        }
                    }
                }
            }
            oList1.add(oList2);
        }
        oList1.add(0, oSet1);
        writer.write(oList1);
        return writer;
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/8
     * @Descripate 导入商户汇款单
     **/
    @Override
    public BaseResponse uploadOrderPaymentFiles(MultipartFile file) {
        BaseResponse baseResponse = new BaseResponse();
        ExcelReader reader;
        try {
            reader = ExcelUtil.getReader(file.getInputStream());
        } catch (Exception e) {
            // 当excel内的格式不正确时
            throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
        }

        List<List<Object>> read = reader.read();
        //判断是否超过上传限制
        if (read.size() <= 0) {
            throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
        }
        List<PayOutRequestDTO> payOutRequestDTOs = new ArrayList<>();
        for (int i = 1; i < read.size(); i++) {
            List<Object> objects = read.get(i);
            PayOutRequestDTO payOutRequestDTO = new PayOutRequestDTO();
            payOutRequestDTO.setInstitutionBatchNo(objects.get(0).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
            payOutRequestDTO.setInstitutionId(objects.get(1).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
            payOutRequestDTO.setOrderNo(objects.get(2).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
            payOutRequestDTO.setOrderCurrency(objects.get(3).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
            payOutRequestDTO.setOrderAmount(new BigDecimal(objects.get(4).toString().replaceAll("/(^\\s*)|(\\s*$)/g", "")));
            payOutRequestDTO.setPaymentCurrency(objects.get(5).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
            payOutRequestDTO.setPaymentAmount(new BigDecimal(objects.get(6).toString().replaceAll("/(^\\s*)|(\\s*$)/g", "")));
            payOutRequestDTO.setBankAccountName(objects.get(7).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
            payOutRequestDTO.setBankAccountNumber(objects.get(8).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
            payOutRequestDTO.setCardholder(objects.get(9).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
            payOutRequestDTO.setIssuerId(objects.get(10).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
            payOutRequestDTO.setCountry(objects.get(11).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
            payOutRequestDTO.setAdress(objects.get(12).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
            payOutRequestDTO.setDescription(objects.get(13).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));

            payOutRequestDTOs.add(payOutRequestDTO);

        }
        baseResponse.setCode(EResultEnum.SUCCESS.getCode());
        baseResponse.setData(payOutRequestDTOs);
        return baseResponse;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/9
     * @Descripate 导出汇款单
     **/
    @Override
    public ExcelWriter getOrderPaymentExcelWriter(List<OrderPaymentExportVO> list, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (OrderPaymentExportVO orderPayment : list) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(orderPayment);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("payoutStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("待汇款");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("汇款中");
                            } else if (String.valueOf(oMap.get(s)).equals("3")) {
                                oList2.add("汇款成功");
                            } else if (String.valueOf(oMap.get(s)).equals("4")) {
                                oList2.add("汇款失败");
                            } else {
                                oList2.add("");
                            }
                        } else {
                            oList2.add(oMap.get(s));
                        }
                    }
                }
            }
            oList1.add(oList2);
        }
        oList1.add(0, oSet1);
        writer.write(oList1);
        return writer;
    }

    /**
     * 机构后台导出汇款单用
     *
     * @param list
     * @param clazz
     * @return
     */
    @Override
    public ExcelWriter getInsOrderPaymentExcelWriter(List<OrderPaymentInsExportVO> list, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (OrderPaymentInsExportVO orderPayment : list) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(orderPayment);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("payoutStatus")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("remittance preparing");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("remittance processing");
                            } else if (String.valueOf(oMap.get(s)).equals("3")) {
                                oList2.add("remittance successful");
                            } else if (String.valueOf(oMap.get(s)).equals("4")) {
                                oList2.add("remittance failed");
                            } else {
                                oList2.add("");
                            }
                        } else {
                            oList2.add(oMap.get(s));
                        }
                    }
                }
            }
            oList1.add(oList2);
        }
        oList1.add(0, oSet1);
        writer.write(oList1);
        return writer;
    }

    /**
     * 代理商交易导出
     * @param queryAgencyTradeVOS
     * @param clazz
     * @return
     */
    @Override
    public ExcelWriter exportAgencyTrade(List<QueryAgencyTradeVO> queryAgencyTradeVOS, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (QueryAgencyTradeVO dto : queryAgencyTradeVOS) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(dto);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if ("tradeStatus".equals(s)) {
                            if ("1".equals(String.valueOf((oMap.get(s))))) {
                                oList2.add("Payment Pending");
                            } else if ("2".equals(String.valueOf((oMap.get(s))))) {
                                oList2.add("Payment Processing");
                            } else if ("3".equals(String.valueOf((oMap.get(s))))) {
                                oList2.add("Payment Success");
                            } else if ("4".equals(String.valueOf((oMap.get(s))))) {
                                oList2.add("Payment Fail");
                            } else {
                                oList2.add("");
                            }
                        } else {
                            oList2.add(oMap.get(s));
                        }
                    }
                }
            }
            oList1.add(oList2);
        }
        oList1.add(0, oSet1);
        writer.write(oList1);
        return writer;
    }


    /**
     * 代理商分润导出
     *
     * @param queryAgencyShareBenefitVOS queryAgencyShareBenefitVOS
     * @param clazz
     * @return
     */
    @Override
    public ExcelWriter exportAgencyShareBenefit(List<QueryAgencyShareBenefitVO> queryAgencyShareBenefitVOS, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (QueryAgencyShareBenefitVO dto : queryAgencyShareBenefitVOS) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(dto);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if ("isShare".equals(s)) {
                            if ("1".equals(String.valueOf((oMap.get(s))))) {
                                oList2.add("Stay Share Benefit");
                            } else if ("2".equals(String.valueOf((oMap.get(s))))) {
                                oList2.add("Has Share Benefit");
                            } else {
                                oList2.add("");
                            }
                        } else {
                            oList2.add(oMap.get(s));
                        }
                    }
                }
            }
            oList1.add(oList2);
        }
        oList1.add(0, oSet1);
        writer.write(oList1);
        return writer;
    }


    /**
     * 运营后台分润报表导出
     * @param shareBenefitReportList
     * @param clazz
     * @return
     */
    @Override
    public ExcelWriter exportShareBenefitReport(List<ShareBenefitReportVO> shareBenefitReportList, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (ShareBenefitReportVO dto : shareBenefitReportList) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(dto);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if ("isShare".equals(s)) {
                            if ("1".equals(String.valueOf((oMap.get(s))))) {
                                oList2.add("待分润");
                            } else if ("2".equals(String.valueOf((oMap.get(s))))) {
                                oList2.add("已分润");
                            } else {
                                oList2.add("");
                            }
                        }else if (s.equals("extend1")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("收单");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("付款");
                            } else {
                                oList2.add("");
                            }
                        } else {
                            oList2.add(oMap.get(s));
                        }
                    }
                }
            }
            oList1.add(oList2);
        }
        oList1.add(0, oSet1);
        writer.write(oList1);
        return writer;
    }
}
