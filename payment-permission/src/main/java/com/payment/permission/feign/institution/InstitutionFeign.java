package com.payment.permission.feign.institution;

import com.payment.common.dto.*;
import com.payment.common.entity.Bank;
import com.payment.common.entity.BankIssuerid;
import com.payment.common.response.BaseResponse;
import com.payment.common.vo.AgencyInstitutionVO;
import com.payment.common.vo.ClearAccountVO;
import com.payment.common.vo.ExportBankVO;
import com.payment.permission.feign.institution.impl.InstitutionFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;


@FeignClient(value = "payment-institution", fallback = InstitutionFeignImpl.class)
public interface InstitutionFeign {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 添加机构信息
     **/
    @PostMapping("/institution/addInstitution")
    BaseResponse addInstitution(@RequestBody @ApiParam InstitutionDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 修改机构信息
     **/
    @PostMapping("/institution/updateInstitution")
    BaseResponse updateInstitution(@RequestBody @ApiParam InstitutionDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构信息
     **/
    @PostMapping("/institution/pageFindInstitution")
    BaseResponse pageFindInstitution(@RequestBody @ApiParam InstitutionDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构审核信息列表
     **/
    @PostMapping("/institution/pageFindInstitutionAduit")
    BaseResponse pageFindInstitutionAduit(@RequestBody @ApiParam InstitutionDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构历史信息列表
     **/
    @PostMapping("/institution/pageFindInstitutionHistory")
    BaseResponse pageFindInstitutionHistory(@RequestBody @ApiParam InstitutionDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构Code查询机构信息详情
     **/
    @GetMapping("/institution/getInstitutionInfoByCode")
    BaseResponse getInstitutionInfoByCode(@RequestParam("institutionCode") @ApiParam String institutionCode);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构id查询机构信息
     **/
    @GetMapping("/institution/getInstitutionInfo")
    BaseResponse getInstitutionInfo(@RequestParam("id") @ApiParam String id);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构Id查询机构变更信息详情
     **/
    @GetMapping("/institution/getInstitutionHistoryInfo")
    BaseResponse getInstitutionHistoryInfo(@RequestParam("id") @ApiParam String id);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构Id查询机构审核信息详情
     **/
    @GetMapping("/institution/getInstitutionInfoAudit")
    BaseResponse getInstitutionInfoAudit(@RequestParam("id") @ApiParam String id);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 启用禁用机构
     **/
    @GetMapping("/institution/banInstitution")
    BaseResponse banInstitution(@RequestParam("institutionId") @ApiParam String institutionId, @RequestParam("enabled") @ApiParam Boolean enabled);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 审核机构信息
     **/
    @GetMapping("/institution/auditInstitution")
    BaseResponse auditInstitution(@RequestParam("institutionId") @ApiParam String institutionId, @RequestParam("enabled") @ApiParam Boolean enabled, @RequestParam("remark") @ApiParam String remark);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 添加银行卡信息
     **/
    @PostMapping("/bankcard/addBankCard")
    BaseResponse addBankCard(@RequestBody @ApiParam List<BankCardDTO> bankCardDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 修改银行卡信息
     **/
    @PostMapping("/bankcard/updateBankCard")
    BaseResponse updateBankCard(@RequestBody @ApiParam BankCardDTO bankCardDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询银行卡
     **/
    @PostMapping("/bankcard/pageBankCard")
    BaseResponse pageBankCard(@RequestBody @ApiParam BankCardSearchDTO bankCardSearchDTO);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 根据机构id查询银行卡
     **/
    @GetMapping("/bankcard/selectBankCardByInsId")
    BaseResponse selectBankCardByInsId(@RequestParam("institutionId") @ApiParam String institutionId);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 启用禁用银行卡
     **/
    @GetMapping("/bankcard/banBankCard")
    BaseResponse banBankCard(@RequestParam("bankCardId") @ApiParam String bankCardId, @RequestParam("enabled") @ApiParam Boolean enabled);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构账户信息列表
     **/
    @PostMapping("/account/pageFindAccount")
    BaseResponse pageFindAccount(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO);

    /**
     * @return
     * @Author ShenXinRan
     * @Date 2019/8/26
     * @Descripate 分页查询代理机构账户信息列表
     **/
    @PostMapping("/account/pageFindAgentAccount")
    BaseResponse pageFindAgentAccount(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO);

    /**
     * @return
     * @Author ShenXinRan
     * @Date 2019/8/26
     * @Descripate 导出代理机构账户信息列表
     *
     * @param accountSearchDTO*/
    @PostMapping("/account/exportAgentAccount")
    BaseResponse exportAgentAccount(@RequestBody @ApiParam AccountSearchExportDTO accountSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 查询冻结余额流水详情
     **/
    @PostMapping("/account/pageFrozenLogs")
    BaseResponse pageFrozenLogs(@RequestBody @ApiParam FrozenMarginInfoDTO accountSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 查询结算户余额流水详情
     **/
    @PostMapping("/account/pageSettleLogs")
    BaseResponse pageSettleLogs(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO);

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/7/17
     * @Descripate 查询清算户余额流水详情
     **/
    @PostMapping("/account/pageClearLogs")
    BaseResponse pageClearLogs(@RequestBody @ApiParam ClearSearchDTO clearSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 导出冻结余额流水详情
     **/
    @PostMapping("/account/exportFrozenLogs")
    BaseResponse exportFrozenLogs(@RequestBody @ApiParam FrozenMarginInfoDTO accountSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 导出结算户余额流水详情
     **/
    @PostMapping("/account/exportSettleLogs")
    BaseResponse exportSettleLogs(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 导出结算户余额流水详情
     **/
    @PostMapping("/account/exportClearLogs")
    List<ClearAccountVO> exportClearLogs(@RequestBody @ApiParam ClearSearchDTO clearSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 导出机构账户信息列表
     **/
    @PostMapping("/account/exportAccountList")
    BaseResponse exportAccountList(@RequestBody @ApiParam AccountSearchDTO accountSearchDTO);

    /**
     * 导出Excel
     *
     * @param institutionDTO
     * @return
     */
    @PostMapping(value = "/institution/export")
    BaseResponse exportInformation(@RequestBody @ApiParam InstitutionDTO institutionDTO);

    /**
     * 通过机构名称查询机构信息
     *
     * @param institutionDTO
     * @return
     */
    @PostMapping(value = "/institution/getInstitutionInfoByName")
    BaseResponse getInstitutionInfoByName(@RequestBody @ApiParam InstitutionDTO institutionDTO);

    /**
     * 设置默认银行卡
     *
     * @param bankCardId
     * @param defaultFlag
     * @return
     */
    @GetMapping("/bankcard/defaultBankCard")
    BaseResponse defaultBankCard(@RequestParam("bankCardId") @ApiParam String bankCardId, @RequestParam("defaultFlag") @ApiParam Boolean defaultFlag);

    /**
     * 导入银行对照信息
     *
     * @param biList
     * @return
     */
    @PostMapping("/bankcard/importBankIssureId")
    BaseResponse importBankIssureId(List<BankIssuerid> biList);

    /**
     * 导入银行信息
     *
     * @param banks
     * @return
     */
    @PostMapping("/bankcard/importBank")
    BaseResponse importBank(@RequestBody @ApiParam List<Bank> banks);

    /**
     * 修改账户自动结算结算开关 最小起结金额
     *
     * @param accountSettleDTO
     * @return
     */
    @PostMapping("/account/updateAccountSettle")
    BaseResponse updateAccountSettle(@RequestBody @Valid @ApiParam AccountSettleDTO accountSettleDTO);


    /**
     * 代理商下拉框信息一览
     *
     * @param institutionDTO
     * @return
     */
    @PostMapping(value = "/institution/getAgencyList")
    BaseResponse getAgencyList(@RequestBody @ApiParam InstitutionDTO institutionDTO);

    /**
     * 代理商商户信息查询
     *
     * @param queryAgencyInstitutionDTO queryAgencyInstitutionDTO
     * @return
     */
    @PostMapping(value = "/institution/getAgencyInstitution")
    BaseResponse getAgencyInstitution(@RequestBody @ApiParam @Valid QueryAgencyInstitutionDTO queryAgencyInstitutionDTO);

    /**
     * 代理商商户信息导出
     *
     * @param exportAgencyInstitutionDTO queryAgencyInstitutionDTO
     * @return
     */
    @PostMapping(value = "/institution/exportAgencyInstitution")
    List<AgencyInstitutionVO> exportAgencyInstitution(@RequestBody @ApiParam @Valid ExportAgencyInstitutionDTO exportAgencyInstitutionDTO);

    /**
     * 导出银行信息
     *
     * @param exportBankDTO exportBankDTO
     * @return
     */
    @PostMapping(value = "/bankcard/exportBank")
    List<ExportBankVO> exportBank(ExportBankDTO exportBankDTO);

}
