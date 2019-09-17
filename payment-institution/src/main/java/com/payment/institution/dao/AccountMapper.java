package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.AccountSearchDTO;
import com.payment.common.dto.AccountSearchExportDTO;
import com.payment.common.entity.Account;
import com.payment.common.vo.AccountListVO;
import com.payment.common.vo.AgentAccountListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountMapper extends BaseMapper<Account> {
    /**
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据币种和机构id查询数量
     * @return
     **/
    @Select("select count(1) from account where institution_id = #{institutionId} and currency = #{currency}")
    int getCountByinstitutionIdAndCurry(@Param("institutionId") String institutionId, @Param("currency") String currency);

    /**
     * @Author YangXu
     * @Date 2019/3/5
     * @Descripate 分页查询机构账户信息列表
     * @return
     **/
    List<AccountListVO> pageFindAccount(AccountSearchDTO accountSearchDTO);

    /**
     * @Author YangXu
     * @Date 2019/3/5
     * @Descripate 导出机构账户信息列表
     * @return
     *
     * @param accountSearchDTO*/
    List<AccountListVO> exportAccountList(AccountSearchExportDTO accountSearchDTO);

    /**
     * 根据机构code和币种获取账户信息
     * @param institutionId
     * @param currency
     * @return
     */
    @Select("select account_code from account where institution_id = #{institutionId} and currency = #{currency}")
    String getAccountCode(@Param("institutionId") String institutionId, @Param("currency") String currency);

    /**
     * 分页查询代理商账户信息列表
     *
     * @param accountSearchDTO
     * @return
     */
    List<AgentAccountListVO> pageFindAgentAccount(AccountSearchDTO accountSearchDTO);

    /**
     * 导出代理商
     *
     * @param accountSearchDTO
     * @return
     */
    List<AgentAccountListVO> exportAgent(AccountSearchExportDTO accountSearchDTO);
}
