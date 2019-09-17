package com.payment.permission.service.impl;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.entity.TradeCheckAccount;
import com.payment.common.utils.BeanToMapUtil;
import com.payment.common.utils.ReflexClazzUtils;
import com.payment.common.vo.ExportTradeAccountVO;
import com.payment.common.vo.TradeAccountDetailVO;
import com.payment.common.vo.TradeCheckAccountDetailVO;
import com.payment.permission.service.InstitutionAccountFeignService;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class InstitutionAccountFeignServiceImpl implements InstitutionAccountFeignService {

    /**
     * Excel 导出功能
     *
     * @param exportTradeAccountVO 机构交易对账输出实体
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getExcelWriter(ExportTradeAccountVO exportTradeAccountVO, String language, Class clazz1, Class clazz2) {
        //获取属性名的名称与注释Map
        Map<String, String[]> totalResult = ReflexClazzUtils.getFiledStructMap(clazz1);
        Map<String, String[]> detailResult = ReflexClazzUtils.getFiledStructMap(clazz2);
        //总表注释信息
        String[] totalComment = totalResult.get(AsianWalletConstant.EXCEL_TITLES);
        //总表属性名信息
        String[] totalProperty = totalResult.get(AsianWalletConstant.EXCEL_ATTRS);
        //详细表注释信息
        String[] detailComment = detailResult.get(AsianWalletConstant.EXCEL_TITLES);
        //详细表属性名信息
        String[] detailProperty = detailResult.get(AsianWalletConstant.EXCEL_ATTRS);
        ExcelWriter writer = ExcelUtil.getBigWriter();
        writer.renameSheet("Deals Total Table");
        //总表信息
        List<TradeCheckAccount> totals = exportTradeAccountVO.getTradeCheckAccounts();
        //详细表信息
        List<TradeAccountDetailVO> details = exportTradeAccountVO.getTradeAccountDetailVOS();
        //总表数据集合
        List<Object> totalDataList = new ArrayList<>();
        //总表注释名称集合
        LinkedHashSet<Object> totalCommentSet = new LinkedHashSet<>();
        //总表
        for (TradeCheckAccount tradeCheckAccount : totals) {
            //将对象的属性名与属性值转换成Map
            HashMap<String, Object> entityMap = BeanToMapUtil.beanToMap(tradeCheckAccount);
            //属性名称集合
            Set<String> propertyNameSet = entityMap.keySet();
            //属性值集合
            ArrayList<Object> attrValueList = new ArrayList<>();
            for (int i = 0; i < totalProperty.length; i++) {
                for (String property : propertyNameSet) {
                    //属性名称相等时
                    if (property.equals(totalProperty[i])) {
                        //添加注释名称信息
                        totalCommentSet.add(totalComment[i]);
                        //添加对应属性名称的属性值
                        attrValueList.add(entityMap.get(property));
                    }
                }
            }
            //添加属性值集合到Excel数据集合中
            totalDataList.add(attrValueList);
        }
        //添加总表注释名称信息
        totalDataList.add(0, totalCommentSet);
        if (AsianWalletConstant.EN_US.equals(language)) {
            writer.merge(10, "Statement: a breakdown of all successful transactions from the previous day (including transactions & refunds)", true);
        } else {
            writer.merge(10, "对账单:统计前一天所有成功交易的明细（包含交易&退款）", true);
        }
        writer.write(totalDataList);


        //详细表
        for (TradeAccountDetailVO tradeAccountDetailVO : details) {
            //详细表数据集合
            List<Object> detailDataList = new ArrayList<>();
            //详细表注释名称集合
            LinkedHashSet<Object> detailCommentSet = new LinkedHashSet<>();
            //添加详细表注释名称信息
            detailDataList.add(detailCommentSet);
            //设置新Sheet
            writer.setSheet(tradeAccountDetailVO.getOrderCurrency());
            for (TradeCheckAccountDetailVO tradeCheckAccountDetailVO : tradeAccountDetailVO.getTradeCheckAccountDetailVOS()) {
                List<Object> attrValueList = new ArrayList<>();
                //将对象的属性名与属性值转换成Map
                HashMap<String, Object> entityMap = BeanToMapUtil.beanToMap(tradeCheckAccountDetailVO);
                Set<String> propertySet = entityMap.keySet();
                for (int i = 0; i < detailProperty.length; i++) {
                    for (String property : propertySet) {
                        //属性名称相同
                        if (property.equals(detailProperty[i])) {
                            detailCommentSet.add(detailComment[i]);
                            if (property.equals("tradeType")) {
                                if (String.valueOf(entityMap.get(property)).equals("1")) {
                                    attrValueList.add("Collection");
                                } else if (String.valueOf(entityMap.get(property)).equals("2")) {
                                    attrValueList.add("Payment");
                                } else {
                                    attrValueList.add("");
                                }
                            } else if (property.equals("tradeStatus")) {
                                if (String.valueOf(entityMap.get(property)).equals("1")) {
                                    attrValueList.add("Payment Pending");
                                } else if (String.valueOf(entityMap.get(property)).equals("2")) {
                                    attrValueList.add("Payment Processing");
                                } else if (String.valueOf(entityMap.get(property)).equals("3")) {
                                    attrValueList.add("Payment Success");
                                } else if (String.valueOf(entityMap.get(property)).equals("4")) {
                                    attrValueList.add("Payment Fail");
                                } else if (String.valueOf(entityMap.get(property)).equals("5")) {
                                    attrValueList.add("Payment Expired");
                                } else {
                                    attrValueList.add("");
                                }
                            } else {
                                attrValueList.add(entityMap.get(property));
                            }
                        }
                    }
                }
                detailDataList.add(attrValueList);
            }
            writer.write(detailDataList);
        }
        return writer;
    }
}
