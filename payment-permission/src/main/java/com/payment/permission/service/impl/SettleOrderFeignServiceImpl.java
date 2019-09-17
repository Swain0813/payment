package com.payment.permission.service.impl;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.entity.SettleOrder;
import com.payment.common.utils.BeanToMapUtil;
import com.payment.common.utils.ReflexClazzUtils;
import com.payment.permission.dto.SettleOrderExport;
import com.payment.permission.service.SettleOrderFeignService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shenxinran
 * @Date: 2019/6/4 11:55
 * @Description: 机构结算
 */
@Service
public class SettleOrderFeignServiceImpl implements SettleOrderFeignService {

    /**
     * 机构结算表的导出
     *
     * @param settleOrders
     * @param settleOrderExportClass
     * @return
     */
    @Override
    public ExcelWriter getExcelWriter(ArrayList<SettleOrder> settleOrders, Class<SettleOrderExport> settleOrderExportClass) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(settleOrderExportClass);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (SettleOrder st : settleOrders) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(st);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int p = 0; p < property.length; p++) {
                for (String s : keySet) {
                    if (s.equals(property[p])) {
                        oSet1.add(comment[p]);
                        if (s.equals("tradeStatus")) {
                            if ((String.valueOf((oMap.get(s))).equals("1"))) {
                                oList2.add("结算中");
                            } else if ((String.valueOf((oMap.get(s))).equals("2"))) {
                                oList2.add("结算成功");
                            } else {
                                oList2.add("结算失败");
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
        //合并单元格
        Map<String, Long> collect = settleOrders.stream()
                .collect(Collectors.groupingBy(SettleOrder::getBatchNo, Collectors.counting()));
        LinkedHashMap<String, SettleOrder> map = new LinkedHashMap<>();
        settleOrders.forEach(v -> {
            if (!map.containsKey(v.getBatchNo())) {
                map.put(v.getBatchNo(), v);
            }
        });
        final Set<String> batchNos = map.keySet();
        //起始行号 0为标题行
        int i = 1;
        for (String batchNo : batchNos) {
            //当某批次号只有一条时，此批次号跳过
            if (1 == Math.toIntExact(collect.get(batchNo))) {
                i++;
                continue;
            }
            //合并列
            //交易手续费
            writer.merge(i, i + Math.toIntExact(collect.get(batchNo)) - 1, 14, 14, map.get(batchNo).getTradeFee(), false);
            //批次总结算金额
            writer.merge(i, i + Math.toIntExact(collect.get(batchNo)) - 1, 17, 17, map.get(batchNo).getTotalSettleAmount(), false);
            //行数递增
            i = i + Math.toIntExact(collect.get(batchNo));
        }
        return writer;
    }

    /**
     * 机构后台用机构结算表导出
     *
     * @param settleOrders
     * @param clazz
     * @return
     */
    @Override
    public ExcelWriter getInsExcelWriter(ArrayList<SettleOrder> settleOrders, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (SettleOrder st : settleOrders) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(st);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int p = 0; p < property.length; p++) {
                for (String s : keySet) {
                    if (s.equals(property[p])) {
                        oSet1.add(comment[p]);
                        if (s.equals("tradeStatus")) {
                            if ((String.valueOf((oMap.get(s))).equals("1"))) {
                                oList2.add("Settlement In Progress");
                            } else if ((String.valueOf((oMap.get(s))).equals("2"))) {
                                oList2.add("Settlement Success");
                            } else {
                                oList2.add("Settlement Failure");
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
        //合并单元格
        Map<String, Long> collect = settleOrders.stream()
                .collect(Collectors.groupingBy(SettleOrder::getBatchNo, Collectors.counting()));
        LinkedHashMap<String, SettleOrder> map = new LinkedHashMap<>();
        settleOrders.forEach(v -> {
            if (!map.containsKey(v.getBatchNo())) {
                map.put(v.getBatchNo(), v);
            }
        });
        //起始行号 0为标题行
        int i = 1;
        final Set<String> batchNos = map.keySet();
        for (String batchNo : batchNos) {
            //当某批次号只有一条时，此批次号跳过
            if (1 == Math.toIntExact(collect.get(batchNo))) {
                i++;
                continue;
            }
            //合并列
            //交易手续费
            writer.merge(i, i + Math.toIntExact(collect.get(batchNo)) - 1, 12, 12, map.get(batchNo).getTradeFee(), false);
            //批次总结算金额
            writer.merge(i, i + Math.toIntExact(collect.get(batchNo)) - 1, 15, 15, map.get(batchNo).getTotalSettleAmount(), false);
            //行数递增
            i = i + Math.toIntExact(collect.get(batchNo));
        }
        return writer;
    }
}
