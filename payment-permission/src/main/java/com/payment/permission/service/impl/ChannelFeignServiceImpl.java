package com.payment.permission.service.impl;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.entity.BankIssuerid;
import com.payment.common.utils.ReflexClazzUtils;
import com.payment.common.vo.ChannelExportVO;
import com.payment.permission.service.ChannelFeignService;
import org.springframework.stereotype.Service;
import java.util.*;
import static com.payment.common.utils.BeanToMapUtil.beanToMap;

/**
 * 通道导出功能的excel的实现类
 */
@Service
public class ChannelFeignServiceImpl implements ChannelFeignService {

    /**
     * @param channels
     * @param clazz
     * @return
     */
    @Override
    public ExcelWriter getChannelsExcelWriter(List<ChannelExportVO> channels, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (ChannelExportVO channel : channels) {
            HashMap<String, Object> oMap = beanToMap(channel);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("channelConnectMethod")) {
                            if (String.valueOf(oMap.get(s)).equals("1")) {
                                oList2.add("直连");
                            } else if (String.valueOf(oMap.get(s)).equals("2")) {
                                oList2.add("间连");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("enabled")) {
                            if ((String.valueOf((oMap.get(s)))).equals("true")) {
                                oList2.add("启用");
                            } else if ((String.valueOf((oMap.get(s)))).equals("false")) {
                                oList2.add("禁用");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("channelFeeType")) {
                            if ((String.valueOf((oMap.get(s)))).equals("dic_7_1")) {
                                oList2.add("单笔费率");
                            } else if ((String.valueOf((oMap.get(s)))).equals("dic_7_2")) {
                                oList2.add("单笔定额");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("supportRefundState")) {
                            if ((String.valueOf((oMap.get(s)))).equals("true")) {
                                oList2.add("支持");
                            } else if ((String.valueOf((oMap.get(s)))).equals("false")) {
                                oList2.add("不支持");
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
     * 映射表导出
     *
     * @param bankIssuerids
     * @param clazz
     * @return
     */
    @Override
    public ExcelWriter getBankIssuerWriter(List<BankIssuerid> bankIssuerids, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (BankIssuerid bankIssuerid : bankIssuerids) {
            HashMap<String, Object> oMap = beanToMap(bankIssuerid);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        oList2.add(oMap.get(s));
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
