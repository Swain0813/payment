package com.payment.institution.service;

import com.payment.common.base.BaseService;
import com.payment.common.dto.*;
import com.payment.common.entity.Account;
import com.payment.common.entity.TmMerChTvAcctBalance;
import com.payment.common.vo.AccountListVO;
import com.payment.common.vo.AgentAccountListVO;
import com.payment.common.vo.ClearAccountVO;
import com.payment.common.vo.FrozenMarginInfoVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface AccountService extends BaseService<Account> {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/5
     * @Descripate 分页查询机构账户信息列表
     **/
    PageInfo<AccountListVO> pageFindAccount(AccountSearchDTO accountSearchDTO);

    /**
     * @param accountSearchDTO
     * @return
     * @Author YangXu
     * @Date 2019/3/5
     * @Descripate 导出机构账户信息列表
     */
    List<AccountListVO> exportAccountList(AccountSearchDTO accountSearchDTO);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 查询冻结余额流水详情
     **/
    PageInfo<FrozenMarginInfoVO> pageFrozenLogs(FrozenMarginInfoDTO frozenMarginInfoDTO);

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/7/17
     * @Descripate 查询清算户余额流水详情
     **/
    PageInfo<ClearAccountVO> pageClearLogs(ClearSearchDTO clearSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 查询结算户余额流水详情
     **/
    PageInfo<TmMerChTvAcctBalance> pageSettleLogs(AccountSearchDTO accountSearchDTO);

    /**
     * @param accountSearchDTO
     * @return
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 导出冻结余额流水详情
     */
    List<FrozenMarginInfoVO> exportFrozenLogs(FrozenMarginInfoDTO accountSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 导出结算户余额流水详情
     **/
    List<TmMerChTvAcctBalance> exportSettleLogs(AccountSearchDTO accountSearchDTO);

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/3/12
     * @Descripate 导出清算户余额流水详情
     **/
    List<ClearAccountVO> exportClearLogs(ClearSearchDTO clearSearchDTO);

    /**
     * 修改账户自动结算结算开关 最小起结金额
     *
     * @param accountSettleDTO
     * @return
     */
    int updateAccountSettle(AccountSettleDTO accountSettleDTO);

    /**
     * 分页查询代理商账户信息列表
     *
     * @param accountSearchDTO
     * @return
     */
    PageInfo<AgentAccountListVO> pageFindAgentAccount(AccountSearchDTO accountSearchDTO);

    /**
     * 导出代理商账户
     *
     * @param accountSearchDTO
     * @return
     */
    List<AgentAccountListVO> exportAgentAccount(AccountSearchExportDTO accountSearchDTO);
}
