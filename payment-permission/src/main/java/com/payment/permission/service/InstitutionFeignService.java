package com.payment.permission.service;

import cn.hutool.poi.excel.ExcelWriter;
import com.payment.common.entity.*;
import com.payment.common.vo.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shenxinran
 * @Date: 2019/3/1 18:15
 * @Description: 机构FeignService
 */
public interface InstitutionFeignService {
    /**
     * Excel 导出功能
     *
     * @param institutions 对象集合
     * @param clazz        类名Class对象
     * @return ExcelWriter writer
     */
    ExcelWriter getExcelWriter(List<InstitutionExportVO> institutions, Class clazz);

    /**
     * Excel 导出账户功能
     *
     * @param institutions 对象集合
     * @param clazz        类名Class对象
     * @return ExcelWriter writer
     */
    ExcelWriter getAccountExcelWriter(List<AccountListVO> institutions, Class clazz);

    /**
     * Excel 导出冻结余额流水详情
     *
     * @param institutions 对象集合
     * @param clazz        类名Class对象
     * @return ExcelWriter writer
     */
    ExcelWriter getFrozenLogsWriter(List<FrozenMarginInfoVO> institutions, Class clazz);

    /**
     * Excel 导出结算户余额流水详情
     *
     * @param institutions 对象集合
     * @param clazz        类名Class对象
     * @return ExcelWriter writer
     */
    ExcelWriter getTmMerChTvAcctBalanceWriter(List<TmMerChTvAcctBalance> institutions, Class clazz);

    /**
     * Excel 导出清算户余额流水详情
     *
     * @param clearAccountVOS 对象集合
     * @param clazz           类名Class对象
     * @return ExcelWriter writer
     */
    ExcelWriter getClearBalanceWriter(List<ClearAccountVO> clearAccountVOS, Class clazz);


    /**
     * Excel 导出机构产品功能
     *
     * @param insPros 对象集合
     * @param clazz   类名Class对象
     * @return ExcelWriter writer
     */
    ExcelWriter getInsproExcelWriter(List<InsProExportVO> insPros, Class clazz);

    /**
     * Excel 导出机构产品功能
     *
     * @param insPros 对象集合
     * @param clazz   类名Class对象
     * @return ExcelWriter writer
     */
    ExcelWriter getInsproLimitExcelWriter(List<InsProExportLimitVO> insPros, Class clazz);

    /**
     * Excel 导出渠道对账详情
     *
     * @param insPros 对象集合
     * @param clazz   类名Class对象
     * @return ExcelWriter writer
     */
    ExcelWriter getCheckAccountWriter(List<CheckAccountVO> insPros, Class clazz);

    /**
     * Excel 导出渠道对账复核详情
     *
     * @param insPros 对象集合
     * @param clazz   类名Class对象
     * @return ExcelWriter writer
     */
    ExcelWriter getCheckAccountAuditWriter(List<CheckAccountAuditVO> insPros, Class clazz);


    /**
     * Excel 导出结算单1
     *
     * @param insPros 对象集合
     * @param clazz   类名Class对象
     * @return ExcelWriter writer
     */
    ExcelWriter getSettleCheckAccountsWriter(ExcelWriter write, String language, List<SettleCheckAccount> insPros, Class clazz);


    /**
     * Excel 导出结算单2
     *
     * @param write
     * @param insPros
     * @param clazz
     * @return
     */
    ExcelWriter getSettleCheckAccountDetailWriter(ExcelWriter write, List<ExportSettleCheckAccountDetailVO> insPros, Class clazz);


    /**
     * Excel 导出产品
     *
     * @param list  对象集合
     * @param clazz 类名Class对象
     * @return ExcelWriter writer
     */
    ExcelWriter getProductExcelWriter(List<ExportProductVO> list, Class<ExportProductVO> clazz);

    /**
     * 导出资金变动即调账记录表的数据
     *
     * @param dtos
     * @param reconciliationDTOClass
     * @return
     */
    ExcelWriter getExportReconciliationWriter(ArrayList<ReconciliationExport> dtos, Class<ReconciliationExport> reconciliationDTOClass);

    /**
     * 导入银行 IssuerId信息
     *
     * @param file
     * @param name
     * @return
     */
    List<BankIssuerid> importBankIssureId(MultipartFile file, String name);

    /**
     * 导入银行
     *
     * @param file
     * @param username
     * @return
     */
    List<Bank> importBank(MultipartFile file, String username);

    /**
     * 导出代理商商户
     *
     * @param agencyInstitutionVOS agencyInstitutionVOS
     * @param clazz                clazz
     * @return
     */
    ExcelWriter exportAgencyInstitution(List<AgencyInstitutionVO> agencyInstitutionVOS, Class clazz);

    /**
     * 导出代理商商户账户信息
     *
     * @param agentAccountListVOS
     * @param clazz
     * @return
     */
    ExcelWriter getAgentAccountWriter(List<AgentAccountListVO> agentAccountListVOS, Class clazz);

    /**
     * 导出银行信息
     *
     * @param exportBankVOS exportBankVOS
     * @return
     */
    ExcelWriter exportBank(List<ExportBankVO> exportBankVOS, Class clazz);

    /**
     * 导出机构通道
     *
     * @param productChannelVOS
     * @param clazz
     * @return
     */
    ExcelWriter getProductChannelExcelWriter(ArrayList<ProductChannelVO> productChannelVOS, Class clazz);

}
