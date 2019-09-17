package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.*;
import com.payment.common.entity.Bank;
import com.payment.common.entity.BankIssuerid;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.vo.AgencyInstitutionVO;
import com.payment.common.vo.ClearAccountVO;
import com.payment.common.vo.ExportBankVO;
import com.payment.permission.feign.institution.InstitutionFeign;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-04 17:35
 **/
@Component
public class InstitutionFeignImpl implements InstitutionFeign {


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 添加机构信息
     **/
    @Override
    public BaseResponse addInstitution(InstitutionDTO institutionDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return 修改机构信息
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate
     **/
    @Override
    public BaseResponse updateInstitution(InstitutionDTO institutionDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构信息
     **/
    @Override
    public BaseResponse pageFindInstitution(InstitutionDTO institutionDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构审核信息列表
     **/
    @Override
    public BaseResponse pageFindInstitutionAduit(InstitutionDTO institutionDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构历史信息列表
     **/
    @Override
    public BaseResponse pageFindInstitutionHistory(InstitutionDTO institutionDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getInstitutionInfoByCode(String institutionCode) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构id查询机构信息
     **/
    @Override
    public BaseResponse getInstitutionInfo(String id) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构id查询机构信息
     **/
    @Override
    public BaseResponse getInstitutionHistoryInfo(String id) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构Id查询机构审核信息详情
     **/
    @Override
    public BaseResponse getInstitutionInfoAudit(String id) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 启用禁用机构
     **/
    @Override
    public BaseResponse banInstitution(String institutionId, Boolean enabled) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 审核机构信息
     **/
    @Override
    public BaseResponse auditInstitution(String institutionId, Boolean enabled, String remark) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 添加银行卡信息
     **/
    @Override
    public BaseResponse addBankCard(List<BankCardDTO> bankCardDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 修改银行卡信息
     **/
    @Override
    public BaseResponse updateBankCard(BankCardDTO bankCardDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询银行卡
     **/
    @Override
    public BaseResponse pageBankCard(BankCardSearchDTO bankCardSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 根据机构id查询银行卡
     **/
    @Override
    public BaseResponse selectBankCardByInsId(String institutionId) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 启用禁用银行卡
     **/
    @Override
    public BaseResponse banBankCard(String bankCardId, Boolean enabled) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构账户信息列表
     **/
    @Override
    public BaseResponse pageFindAccount(AccountSearchDTO accountSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @param accountSearchDTO
     * @return
     * @Author ShenXinRan
     * @Date 2019/8/26
     * @Descripate 分页查询代理机构账户信息列表
     */
    @Override
    public BaseResponse pageFindAgentAccount(AccountSearchDTO accountSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @param accountSearchDTO
     * @return
     * @Author ShenXinRan
     * @Date 2019/8/26
     * @Descripate 导出代理机构账户信息列表
     */
    @Override
    public BaseResponse exportAgentAccount(AccountSearchExportDTO accountSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 查询冻结余额流水详情
     **/
    @Override
    public BaseResponse pageFrozenLogs(FrozenMarginInfoDTO accountSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 查询结算户余额流水详情
     **/
    @Override
    public BaseResponse pageSettleLogs(AccountSearchDTO accountSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse exportFrozenLogs(FrozenMarginInfoDTO accountSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse exportSettleLogs(AccountSearchDTO accountSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 导出机构账户信息列表
     **/
    @Override
    public BaseResponse exportAccountList(AccountSearchDTO accountSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse exportInformation(InstitutionDTO institutionDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getInstitutionInfoByName(InstitutionDTO institutionDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 设置默认银行卡
     *
     * @param bankCardId
     * @param defaultFlag
     * @return
     */
    @Override
    public BaseResponse defaultBankCard(String bankCardId, Boolean defaultFlag) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 导入银行issuerId映射表
     *
     * @param biList
     * @return
     */
    @Override
    public BaseResponse importBankIssureId(List<BankIssuerid> biList) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 导入银行信息
     *
     * @param banks
     * @return
     */
    @Override
    public BaseResponse importBank(List<Bank> banks) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageClearLogs(ClearSearchDTO clearSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public List<ClearAccountVO> exportClearLogs(ClearSearchDTO clearSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 修改账户自动结算结算开关 最小起结金额
     *
     * @param accountSettleDTO
     * @return
     */
    @Override
    public BaseResponse updateAccountSettle(@Valid AccountSettleDTO accountSettleDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 代理商下拉框信息一览
     *
     * @param institutionDTO
     * @return
     */
    @Override
    public BaseResponse getAgencyList(InstitutionDTO institutionDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 代理商商户信息查询
     *
     * @param queryAgencyInstitutionDTO
     * @return
     */
    @Override
    public BaseResponse getAgencyInstitution(@Valid QueryAgencyInstitutionDTO queryAgencyInstitutionDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 代理商商户信息导出
     *
     * @param exportAgencyInstitutionDTO
     * @return
     */
    @Override
    public List<AgencyInstitutionVO> exportAgencyInstitution(@Valid ExportAgencyInstitutionDTO exportAgencyInstitutionDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public List<ExportBankVO> exportBank(ExportBankDTO exportBankDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
