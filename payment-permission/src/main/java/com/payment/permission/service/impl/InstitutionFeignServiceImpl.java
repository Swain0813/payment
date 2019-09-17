package com.payment.permission.service.impl;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.entity.*;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.BeanToMapUtil;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.IDS;
import com.payment.common.utils.ReflexClazzUtils;
import com.payment.common.vo.*;
import com.payment.permission.dao.BankIssueridMapper;
import com.payment.permission.dao.BankMapper;
import com.payment.permission.dao.ChannelMapper;
import com.payment.permission.service.InstitutionFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * @author shenxinran
 * @Date: 2019/3/1 18:16
 * @Description: 机构FeignService
 */
@Service
@Slf4j
public class InstitutionFeignServiceImpl implements InstitutionFeignService {

    @Autowired
    private BankIssueridMapper bankIssueridMapper;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private BankMapper bankMapper;

    /**
     * Excel 导出功能
     *
     * @param institutionExportVOS 对象集合
     * @param clazz                类名Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getExcelWriter(List<InstitutionExportVO> institutionExportVOS, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (InstitutionExportVO institutionExportVO : institutionExportVOS) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(institutionExportVO);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int p = 0; p < property.length; p++) {
                for (String s : keySet) {
                    if (s.equals(property[p])) {
                        oSet1.add(comment[p]);
                        if (s.equals("auditStatus")) {
                            if ((String.valueOf((oMap.get(s))).equals("1"))) {
                                oList2.add("待审核");
                            } else if ((String.valueOf((oMap.get(s))).equals("2"))) {
                                oList2.add("审核通过");
                            } else if ((String.valueOf((oMap.get(s))).equals("3"))) {
                                oList2.add("审核不通过");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("enabled")) {
                            if ((String.valueOf((oMap.get(s)))).equals("true")) {
                                oList2.add("启用");
                            } else if ((String.valueOf((oMap.get(s)))).equals("false")) {
                                oList2.add("禁用");
                            }
                        } else if (s.equals("institutionType")) {
                            if ((String.valueOf((oMap.get(s)))).equals("1")) {
                                oList2.add("机构");
                            } else if ((String.valueOf((oMap.get(s)))).equals("2")) {
                                oList2.add("代理商");
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
     * Excel 导出账户功能
     *
     * @param list  对象集合
     * @param clazz 类名Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getAccountExcelWriter(List<AccountListVO> list, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (AccountListVO accountListVO : list) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(accountListVO);
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

    /**
     * Excel 导出冻结余额流水详情
     *
     * @param list  对象集合
     * @param clazz 类名Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getFrozenLogsWriter(List<FrozenMarginInfoVO> list, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (FrozenMarginInfoVO tcsFrozenFundsLogs : list) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(tcsFrozenFundsLogs);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("status")) {
                            if ((String.valueOf((oMap.get(s))).equals("5"))) {
                                oList2.add("冻结成功");
                            } else if ((String.valueOf((oMap.get(s))).equals("8"))) {
                                oList2.add("解冻成功");
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
     * Excel 导出结算户余额流水详情
     *
     * @param list  对象集合
     * @param clazz 类名Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getTmMerChTvAcctBalanceWriter(List<TmMerChTvAcctBalance> list, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (TmMerChTvAcctBalance tmMerChTvAcctBalance : list) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(tmMerChTvAcctBalance);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("tradetype")) {
                            if ((String.valueOf((oMap.get(s))).equals("AA"))) {
                                oList2.add("调账");
                            } else if ((String.valueOf((oMap.get(s))).equals("ST"))) {
                                oList2.add("结算");
                            } else if ((String.valueOf((oMap.get(s))).equals("RV"))) {
                                oList2.add("撤销");
                            } else if ((String.valueOf((oMap.get(s))).equals("RF"))) {
                                oList2.add("退款");
                            } else if ((String.valueOf((oMap.get(s))).equals("NT"))) {
                                oList2.add("收单");
                            } else if ((String.valueOf((oMap.get(s))).equals("WD"))) {
                                oList2.add("提款");
                            } else if ((String.valueOf((oMap.get(s))).equals("TA"))) {
                                oList2.add("转账");
                            } else if ((String.valueOf((oMap.get(s))).equals("FZ"))) {
                                oList2.add("冻结");
                            } else if ((String.valueOf((oMap.get(s))).equals("TW"))) {
                                oList2.add("解冻");
                            } else if ((String.valueOf((oMap.get(s))).equals("PM"))) {
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

    /**
     * Excel 导出清算户余额流水详情
     *
     * @param clearAccountVOS 对象集合
     * @param clazz           类名Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getClearBalanceWriter(List<ClearAccountVO> clearAccountVOS, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (ClearAccountVO clearAccountVO : clearAccountVOS) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(clearAccountVO);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("tradetype")) {
                            if ((String.valueOf((oMap.get(s))).equals("AA"))) {
                                oList2.add("调账");
                            } else if ((String.valueOf((oMap.get(s))).equals("ST"))) {
                                oList2.add("结算");
                            } else if ((String.valueOf((oMap.get(s))).equals("RV"))) {
                                oList2.add("撤销");
                            } else if ((String.valueOf((oMap.get(s))).equals("RF"))) {
                                oList2.add("退款");
                            } else if ((String.valueOf((oMap.get(s))).equals("NT"))) {
                                oList2.add("收单");
                            } else if ((String.valueOf((oMap.get(s))).equals("WD"))) {
                                oList2.add("提款");
                            } else if ((String.valueOf((oMap.get(s))).equals("FZ"))) {
                                oList2.add("冻结");
                            } else if ((String.valueOf((oMap.get(s))).equals("TW"))) {
                                oList2.add("解冻");
                            } else if ((String.valueOf((oMap.get(s))).equals("PM"))) {
                                oList2.add("付款");
                            } else if ((String.valueOf((oMap.get(s))).equals("TA"))) {
                                oList2.add("转账");
                            } else if ((String.valueOf((oMap.get(s))).equals("CL"))) {
                                oList2.add("清算");
                            } else if ((String.valueOf((oMap.get(s))).equals("ST"))) {
                                oList2.add("结算");
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
     * Excel 导出机构产品功能
     *
     * @param insPros 对象集合
     * @param clazz   类名Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getInsproExcelWriter(List<InsProExportVO> insPros, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (InsProExportVO insProExportVO : insPros) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(insProExportVO);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("transType")) {
                            if ((String.valueOf((oMap.get(s)))).equals("1")) {
                                oList2.add("收");
                            } else if ((String.valueOf((oMap.get(s)))).equals("2")) {
                                oList2.add("付");
                            }
                        } else if (s.equals("enabled")) {
                            if ((String.valueOf((oMap.get(s)))).equals("true")) {
                                oList2.add("启用");
                            } else if ((String.valueOf((oMap.get(s)))).equals("false")) {
                                oList2.add("禁用");
                            }
                        } else if (s.equals("dividedMode")) {
                            if ((String.valueOf((oMap.get(s)))).equals("1")) {
                                oList2.add("分成");
                            } else if ((String.valueOf((oMap.get(s)))).equals("2")) {
                                oList2.add("费用差");
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
     * Excel 导出机构产品功能
     *
     * @param insPros 对象集合
     * @param clazz   类名Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getInsproLimitExcelWriter(List<InsProExportLimitVO> insPros, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (InsProExportLimitVO insProExportVO : insPros) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(insProExportVO);
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

    /**
     * Excel 导出渠道对账详情
     *
     * @param insPros 对象集合
     * @param clazz   类名Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getCheckAccountWriter(List<CheckAccountVO> insPros, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (CheckAccountVO checkAccountVO : insPros) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(checkAccountVO);
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

    /**
     * Excel 导出渠道对账详情
     *
     * @param insPros 对象集合
     * @param clazz   类名Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getCheckAccountAuditWriter(List<CheckAccountAuditVO> insPros, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (CheckAccountAuditVO checkAccountVO : insPros) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(checkAccountVO);
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

    /**
     * Excel 导出结算单1
     *
     * @param insPros 对象集合
     * @param clazz   类名Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getSettleCheckAccountsWriter(ExcelWriter writer, String language, List<SettleCheckAccount> insPros, Class clazz) {
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();

        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (SettleCheckAccount settleCheckAccount : insPros) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(settleCheckAccount);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("checkTime")) {
                            oList2.add(DateToolUtils.SHORT_DATE_FORMAT.format(oMap.get(s)));
                        } else {
                            oList2.add(oMap.get(s));
                        }
                    }
                }
            }
            oList1.add(oList2);
        }
        oList1.add(0, oSet1);
        writer.setColumnWidth(-1, 20);
        writer.passRows(1);
        if (AsianWalletConstant.EN_US.equals(language)) {
            writer.merge(0, 0, 0, 7, "Institutional statement: all transactions affecting changes in balance during the previous settlement period", true);
        } else {
            writer.merge(0, 0, 0, 7, "机构结算单:上一个结算周期内影响余额变动的所有交易", true);
        }
        writer.write(oList1);
        return writer;
    }

    /**
     * Excel 导出结算单2
     *
     * @param insPros 对象集合
     * @param clazz   类名Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getSettleCheckAccountDetailWriter(ExcelWriter writer, List<ExportSettleCheckAccountDetailVO> insPros, Class clazz) {
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (ExportSettleCheckAccountDetailVO settleCheckAccountDetail : insPros) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(settleCheckAccountDetail);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("balancetype")) {
                            if ((String.valueOf((oMap.get(s)))).equals("1")) {
                                oList2.add("Normal Money");
                            } else if ((String.valueOf((oMap.get(s)))).equals("2")) {
                                oList2.add("Freeze Funds");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("tradetype")) {
                            if ((String.valueOf((oMap.get(s)))).equals("RF")) {
                                oList2.add("refund");
                            } else if ((String.valueOf((oMap.get(s)))).equals("WD")) {
                                oList2.add("withdrawals");
                            } else if ((String.valueOf((oMap.get(s)))).equals("ST")) {
                                oList2.add("acquire");
                            } else if ((String.valueOf((oMap.get(s)))).equals("PM")) {
                                oList2.add("payment");
                            } else if ((String.valueOf((oMap.get(s)))).equals("AA")) {
                                oList2.add("reconciliation");
                            } else if ((String.valueOf((oMap.get(s)))).equals("RV")) {
                                oList2.add("reverse");
                            } else if ((String.valueOf((oMap.get(s)))).equals("FZ")) {
                                oList2.add("freeze");
                            } else if ((String.valueOf((oMap.get(s)))).equals("TW")) {
                                oList2.add("unfreeze");
                            } else if ((String.valueOf((oMap.get(s)))).equals("PM")) {
                                oList2.add("payment");
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
        writer.setColumnWidth(-1, 20);
        writer.write(oList1);
        return writer;
    }

    /**
     * Excel 导出产品
     *
     * @param list  对象集合
     * @param clazz 类名Class对象
     * @return ExcelWriter writer
     */
    @Override
    public ExcelWriter getProductExcelWriter(List<ExportProductVO> list, Class<ExportProductVO> clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (ExportProductVO exportProductVO : list) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(exportProductVO);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        switch (s) {
                            case "transType":
                                switch (String.valueOf(oMap.get(s))) {
                                    case "1":
                                        oList2.add("收款");
                                        break;
                                    case "2":
                                        oList2.add("付款");
                                        break;
                                    default:
                                        oList2.add("");
                                        break;
                                }
                                break;
                            case "tradeDirection":
                                switch (String.valueOf(oMap.get(s))) {
                                    case "1":
                                        oList2.add("线上PC端");
                                        break;
                                    case "2":
                                        oList2.add("线上移动端");
                                        break;
                                    case "3":
                                        oList2.add("线下移动端");
                                        break;
                                    default:
                                        oList2.add("");
                                        break;
                                }
                                break;
                            default:
                                oList2.add(oMap.get(s));
                                break;
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
     * 导出资金变动即调账记录表的数据
     *
     * @param dtos
     * @param clazz
     * @return
     */
    @Override
    public ExcelWriter getExportReconciliationWriter(ArrayList<ReconciliationExport> dtos, Class<ReconciliationExport> clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (ReconciliationExport dto : dtos) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(dto);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if (s.equals("status")) {//调账状态 1-待调账 2-调账成功 3-调账失败, 4-待冻结 5-冻结成功 6-冻结失败, 7-待解冻 8-解冻成功 9-解冻失败
                            if ((String.valueOf((oMap.get(s)))).equals("5")) {
                                oList2.add("冻结成功");
                            } else if ((String.valueOf((oMap.get(s)))).equals("8")) {
                                oList2.add("解冻成功");
                            } else if ((String.valueOf((oMap.get(s)))).equals("1")) {
                                oList2.add("待调账");
                            } else if ((String.valueOf((oMap.get(s)))).equals("2")) {
                                oList2.add("调账成功");
                            } else if ((String.valueOf((oMap.get(s)))).equals("3")) {
                                oList2.add("调账失败");
                            } else if ((String.valueOf((oMap.get(s)))).equals("4")) {
                                oList2.add("待冻结");
                            } else if ((String.valueOf((oMap.get(s)))).equals("6")) {
                                oList2.add("冻结失败");
                            } else if ((String.valueOf((oMap.get(s)))).equals("7")) {
                                oList2.add("待解冻");
                            } else if ((String.valueOf((oMap.get(s)))).equals("9")) {
                                oList2.add("解冻失败");
                            } else {
                                oList2.add("");
                            }
                        } else if (s.equals("reconciliationType")) {//1-调入,2-调出,3-冻结,4-解冻
                            if ((String.valueOf((oMap.get(s)))).equals("3")) {
                                oList2.add("资金冻结");
                            } else if ((String.valueOf((oMap.get(s)))).equals("4")) {
                                oList2.add("资金解冻");
                            } else if ((String.valueOf((oMap.get(s)))).equals("1")) {
                                oList2.add("资金调入");
                            } else if ((String.valueOf((oMap.get(s)))).equals("2")) {
                                oList2.add("资金调出");
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
     * 导入银行 IssuerId信息
     *
     * @param file
     * @param name
     * @return
     */
    @Override
    public List<BankIssuerid> importBankIssureId(MultipartFile file, String name) {
        ArrayList<BankIssuerid> h = new ArrayList<>();
        String fileName = file.getOriginalFilename();
        // 判断格式0
        if (!fileName.matches("^.+\\.(?i)(xls)$") && !fileName.matches("^.+\\.(?i)(xlsx)$")) {
            throw new BusinessException(EResultEnum.FILE_FORMAT_ERROR.getCode());
        }
        ExcelReader reader;
        try {
            reader = ExcelUtil.getReader(file.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            // 当excel内的格式不正确时
            throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
        }

        List<List<Object>> read = reader.read();
        //判断是否超过上传限制
        if (read.size() - 1 > AsianWalletConstant.UPLOAD_LIMIT) {
            throw new BusinessException(EResultEnum.EXCEEDING_UPLOAD_LIMIT.getCode());
        }
        if (read.size() <= 0) {
            throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
        }
        List<String> channelCode = channelMapper.selectAllChannelCode();
        List<String> bankName = bankMapper.selectAllBankName();
        for (int i = 1; i < read.size(); i++) {
            List<Object> objects = read.get(i);
            BankIssuerid ol = new BankIssuerid();
            //判断传入的excel的格式是否符合约定
            if (StringUtils.isEmpty(objects.get(0))
                    || StringUtils.isEmpty(objects.get(1))
                    || StringUtils.isEmpty(objects.get(2))
                    || objects.size() != 4
                    || StringUtils.isEmpty(objects.get(3))) {
                throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
            }
            String code = objects.get(2).toString().replaceAll("/(^\\s*)|(\\s*$)/g", "");
            String bName = objects.get(0).toString().replaceAll("/(^\\s*)|(\\s*$)/g", "");
            if (!channelCode.contains(code) || !bankName.contains(bName)) {
                log.info("-------导入映射表信息错误--------通道CODE:{},银行名:{}", JSON.toJSONString(code), JSON.toJSONString(bName));
                throw new BusinessException(EResultEnum.CHANNEL_OR_BANK_DOES_NOT_EXIST.getCode());
            }
            try {
                ol.setBankName(bName);
                ol.setCurrency(objects.get(1).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
                ol.setChannelCode(code);
                ol.setIssuerId(objects.get(3).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
            } catch (Exception e) {
                // 当excel内的格式不正确时
                throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
            }
            ol.setId(IDS.uuid2());
            ol.setCreator(name);
            ol.setCreateTime(new Date());
            ol.setEnabled(true);
            if (bankIssueridMapper.findDuplicatesCount(ol) > 0) {
                continue;
            }
            h.add(ol);
        }
        if (h.size() == 0) {
            throw new BusinessException(EResultEnum.IMPORT_REPEAT_ERROR.getCode());
        }
        return h;
    }

    /**
     * 导入银行
     *
     * @param file
     * @param username
     * @return
     */
    @Override
    public List<Bank> importBank(MultipartFile file, String username) {
        ArrayList<Bank> h = new ArrayList<>();
        String fileName = file.getOriginalFilename();
        // 判断格式0
        if (!fileName.matches("^.+\\.(?i)(xls)$") && !fileName.matches("^.+\\.(?i)(xlsx)$")) {
            throw new BusinessException(EResultEnum.FILE_FORMAT_ERROR.getCode());
        }
        ExcelReader reader;
        try {
            reader = ExcelUtil.getReader(file.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            // 当excel内的格式不正确时
            throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
        }

        List<List<Object>> read = reader.read();
        //判断是否超过上传限制
        if (read.size() - 1 > AsianWalletConstant.UPLOAD_LIMIT) {
            throw new BusinessException(EResultEnum.EXCEEDING_UPLOAD_LIMIT.getCode());
        }
        if (read.size() <= 0) {
            throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
        }

        for (int i = 1; i < read.size(); i++) {
            List<Object> objects = read.get(i);

            Bank ol = new Bank();
            //判断传入的excel的格式是否符合约定
            if (StringUtils.isEmpty(objects.get(0))
                    || StringUtils.isEmpty(objects.get(1))
                    || StringUtils.isEmpty(objects.get(2))
                    || objects.size() != 4
                    || StringUtils.isEmpty(objects.get(3))) {
                throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
            }
            try {
                ol.setBankName(objects.get(0).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
                ol.setBankCurrency(objects.get(1).toString().replaceAll("\\s*", ""));
                ol.setBankCountry(objects.get(2).toString().replaceAll("\\s*", ""));
                ol.setIssuerId(objects.get(3).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
            } catch (Exception e) {
                // 当excel内的格式不正确时
                throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
            }
            ol.setId(IDS.uuid2());
            ol.setBankCode("" + IDS.uniqueID());
            ol.setCreator(username);
            ol.setCreateTime(new Date());
            ol.setEnabled(true);
            if (bankMapper.findDuplicatesCount(ol) > 0) {
                continue;
            }
            h.add(ol);
        }
        for (int i = 0; i < h.size() - 1; i++) {
            for (int j = h.size() - 1; j > i; j--) {
                if (h.get(j).getBankName().equals(h.get(i).getBankName()) && h.get(j).getBankCurrency().equals(h.get(i).getBankCurrency())) {
                    h.remove(j);
                }
            }
        }
        if (h.size() == 0) {
            throw new BusinessException(EResultEnum.IMPORT_REPEAT_ERROR.getCode());
        }
        return h;
    }

    /**
     * 导出代理商商户
     *
     * @param agencyInstitutionVOS agencyInstitutionVOS
     * @param clazz                clazz
     * @return
     */
    @Override
    public ExcelWriter exportAgencyInstitution(List<AgencyInstitutionVO> agencyInstitutionVOS, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (AgencyInstitutionVO dto : agencyInstitutionVOS) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(dto);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if ("auditStatus".equals(s)) {
                            if ("1".equals(String.valueOf((oMap.get(s))))) {
                                oList2.add("Check Pending");
                            } else if ("2".equals(String.valueOf((oMap.get(s))))) {
                                oList2.add("Pass The Audit");
                            } else if ("3".equals(String.valueOf((oMap.get(s))))) {
                                oList2.add("Not Approved");
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
     * 导出代理商商户账户信息
     *
     * @param agentAccountListVOS
     * @param clazz
     * @return
     */
    @Override
    public ExcelWriter getAgentAccountWriter(List<AgentAccountListVO> agentAccountListVOS, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (AgentAccountListVO dto : agentAccountListVOS) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(dto);
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

    /**
     * 导出银行信息
     *
     * @param exportBankVOS exportBankVOS
     * @return
     */
    @Override
    public ExcelWriter exportBank(List<ExportBankVO> exportBankVOS, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (ExportBankVO dto : exportBankVOS) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(dto);
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

    /**
     * 导出机构通道
     *
     * @param productChannelVOS
     * @param clazz
     * @return
     */
    @Override
    public ExcelWriter getProductChannelExcelWriter(ArrayList<ProductChannelVO> productChannelVOS, Class clazz) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        Map<String, String[]> result = ReflexClazzUtils.getFiledStructMap(clazz);
        //注释信息
        String[] comment = result.get(AsianWalletConstant.EXCEL_TITLES);
        //属性名信息
        String[] property = result.get(AsianWalletConstant.EXCEL_ATTRS);
        ArrayList<Object> oList1 = new ArrayList<>();
        LinkedHashSet<Object> oSet1 = new LinkedHashSet<>();
        for (ProductChannelVO dto : productChannelVOS) {
            HashMap<String, Object> oMap = BeanToMapUtil.beanToMap(dto);
            ArrayList<Object> oList2 = new ArrayList<>();
            Set<String> keySet = oMap.keySet();
            for (int i = 0; i < property.length; i++) {
                for (String s : keySet) {
                    if (s.equals(property[i])) {
                        oSet1.add(comment[i]);
                        if ("enabled".equals(s)) {
                            if ("true".equalsIgnoreCase(String.valueOf((oMap.get(s))))) {
                                oList2.add("启用");
                            } else if ("false".equalsIgnoreCase(String.valueOf((oMap.get(s))))) {
                                oList2.add("禁用");
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

