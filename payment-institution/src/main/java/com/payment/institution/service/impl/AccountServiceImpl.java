package com.payment.institution.service.impl;

import com.payment.common.base.BaseServiceImpl;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.*;
import com.payment.common.entity.Account;
import com.payment.common.entity.SettleControl;
import com.payment.common.entity.TmMerChTvAcctBalance;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.IDS;
import com.payment.common.vo.*;
import com.payment.institution.dao.AccountMapper;
import com.payment.institution.dao.ReconciliationMapper;
import com.payment.institution.dao.SettleControlMapper;
import com.payment.institution.dao.TmMerChTvAcctBalanceMapper;
import com.payment.institution.service.AccountService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-03-05 16:00
 **/
@Service
public class AccountServiceImpl extends BaseServiceImpl<Account> implements AccountService {


    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private TmMerChTvAcctBalanceMapper tmMerChTvAcctBalanceMapper;

    @Autowired
    private ReconciliationMapper reconciliationMapper;

    @Autowired
    private SettleControlMapper settleControlMapper;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/5
     * @Descripate 分页查询机构账户信息列表
     **/
    @Override

    public PageInfo<AccountListVO> pageFindAccount(AccountSearchDTO accountSearchDTO) {
        List<AccountListVO> accountListVOS = accountMapper.pageFindAccount(accountSearchDTO);
        List<FrozenMarginListVO> frozenMarginList = reconciliationMapper.getFrozenMarginList();
        accountListVOS.forEach(a -> {
            frozenMarginList.forEach(b -> {
                if (a.getId().equals(b.getInstitution_order_id()) && a.getCurrency().equals(b.getOrder_currency())) {
                    if (b.getAccount_type().equals(TradeConstant.ACCOUNT_FREEZE)) {
                        //冻结金额
                        a.setFrozenBalance(b.getAmount());
                    } else {
                        a.setMarginBalance(b.getAmount());
                    }
                }
            });
        });

        return new PageInfo<AccountListVO>(accountListVOS);
    }


    /**
     * @param accountSearchDTO
     * @return
     * @Author YangXu
     * @Date 2019/3/5
     * @Descripate 导出机构账户信息列表
     */
    @Override
    public List<AccountListVO> exportAccountList(AccountSearchDTO accountSearchDTO) {
        List<AccountListVO> accountListVOS = accountMapper.pageFindAccount(accountSearchDTO);
        List<FrozenMarginListVO> frozenMarginList = reconciliationMapper.getFrozenMarginList();
        accountListVOS.forEach(a -> {
            frozenMarginList.forEach(b -> {
                if (a.getId().equals(b.getInstitution_order_id()) && a.getCurrency().equals(b.getOrder_currency())) {
                    if (b.getAccount_type().equals(TradeConstant.ACCOUNT_FREEZE)) {
                        //冻结金额
                        a.setFrozenBalance(b.getAmount());
                    } else {
                        a.setMarginBalance(b.getAmount());
                    }
                }
            });
        });
        return accountListVOS;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 查询冻结余额流水详情
     **/
    @Override
    public PageInfo<FrozenMarginInfoVO> pageFrozenLogs(FrozenMarginInfoDTO frozenMarginInfoDTO) {
        if (StringUtils.isEmpty(frozenMarginInfoDTO.getPageNum()) || StringUtils.isEmpty(frozenMarginInfoDTO.getPageSize())) {
            frozenMarginInfoDTO.setPageNum(1);
            frozenMarginInfoDTO.setPageSize(20);
        }
        List<FrozenMarginInfoVO> frozenMarginInfoVOS = reconciliationMapper.pageFrozenLogs(frozenMarginInfoDTO);
        for (FrozenMarginInfoVO vo : frozenMarginInfoVOS) {
            if (vo.getStatus() == TradeConstant.UNFREEZE_SUCCESS) {
                vo.setAmount(vo.getAmount().negate());
            }
        }
        return new PageInfo<FrozenMarginInfoVO>(frozenMarginInfoVOS);
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 查询结算户余额流水详情
     **/
    @Override
    public PageInfo<TmMerChTvAcctBalance> pageSettleLogs(AccountSearchDTO accountSearchDTO) {
        accountSearchDTO.setSort("balance_timestamp");//变动时间降序
        return new PageInfo(tmMerChTvAcctBalanceMapper.pageAccountBalanceLogs(accountSearchDTO));
    }

    /**
     * @param frozenMarginInfoDTO
     * @return
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 导出冻结余额流水详情
     */
    @Override
    public List<FrozenMarginInfoVO> exportFrozenLogs(FrozenMarginInfoDTO frozenMarginInfoDTO) {
        List<FrozenMarginInfoVO> frozenMarginInfoVOS = reconciliationMapper.pageFrozenLogs(frozenMarginInfoDTO);
        for (FrozenMarginInfoVO vo : frozenMarginInfoVOS) {
            if (vo.getStatus() == TradeConstant.UNFREEZE_SUCCESS) {
                vo.setAmount(vo.getAmount().negate());
            }
        }
        return frozenMarginInfoVOS;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 导出结算户余额流水详情
     **/
    @Override
    public List<TmMerChTvAcctBalance> exportSettleLogs(AccountSearchDTO accountSearchDTO) {
        return tmMerChTvAcctBalanceMapper.exportAccountBalanceLogs(accountSearchDTO);
    }

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/3/12
     * @Descripate 导出清算户余额流水详情
     **/
    @Override
    public List<ClearAccountVO> exportClearLogs(ClearSearchDTO clearSearchDTO) {
        return tmMerChTvAcctBalanceMapper.exportClearBalanceLogs(clearSearchDTO);
    }

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/7/17
     * @Descripate 查询清算户余额流水详情
     **/
    @Override
    public PageInfo<ClearAccountVO> pageClearLogs(ClearSearchDTO clearSearchDTO) {
        clearSearchDTO.setSort("addDatetime");//变动时间降序
        return new PageInfo(tmMerChTvAcctBalanceMapper.pageClearBalanceLogs(clearSearchDTO));
    }

    /**
     * 修改账户自动结算结算开关 最小起结金额
     *
     * @param accountSettleDTO
     * @return
     */
    @Override
    public int updateAccountSettle(AccountSettleDTO accountSettleDTO) {
        accountSettleDTO.setUpdateTime(new Date());
        SettleControl settleControl = settleControlMapper.selectByAccountId(accountSettleDTO.getAccountId());
        if (StringUtils.isEmpty(settleControl)) {
            SettleControl sc = new SettleControl();
            sc.setId(IDS.uuid2());
            sc.setAccountId(accountSettleDTO.getAccountId());
            sc.setEnabled(true);
            sc.setSettleSwitch(true);//默认开启自动结算
            sc.setMinSettleAmount(StringUtils.isEmpty(accountSettleDTO.getMinSettleAmount()) ? accountSettleDTO.getMinSettleAmount() : BigDecimal.ZERO);
            sc.setCreateTime(new Date());
            sc.setCreator(accountSettleDTO.getModifier());
            return settleControlMapper.insert(sc);
        } else {
            BeanUtils.copyProperties(accountSettleDTO, settleControl);
            return settleControlMapper.updateByPrimaryKeySelective(settleControl);
        }
    }


    /**
     * 分页查询代理商账户信息列表
     *
     * @param accountSearchDTO
     * @return
     */
    @Override
    public PageInfo<AgentAccountListVO> pageFindAgentAccount(AccountSearchDTO accountSearchDTO) {
        if (StringUtils.isEmpty(accountSearchDTO.getInstitutionCode())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        List<AgentAccountListVO> AgentAccountListVO = accountMapper.pageFindAgentAccount(accountSearchDTO);
        List<FrozenMarginListVO> frozenMarginList = reconciliationMapper.getFrozenMarginList();
        AgentAccountListVO.forEach(a -> {
            frozenMarginList.forEach(b -> {
                if (a.getId().equals(b.getInstitution_order_id()) && a.getCurrency().equals(b.getOrder_currency())) {
                    if (b.getAccount_type().equals(TradeConstant.ACCOUNT_MARGIN)) {
                        //保证金
                        a.setMarginBalance(b.getAmount());
                    }
                }
            });
        });
        return new PageInfo<AgentAccountListVO>(AgentAccountListVO);
    }

    /**
     * 导出代理商账户
     *
     * @param accountSearchDTO
     * @return
     */
    @Override
    public List<AgentAccountListVO> exportAgentAccount(AccountSearchExportDTO accountSearchDTO) {
        if (StringUtils.isEmpty(accountSearchDTO.getInstitutionCode())) {
            //机构CODE
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        List<AgentAccountListVO> AgentAccountListVO = accountMapper.exportAgent(accountSearchDTO);
        List<FrozenMarginListVO> frozenMarginList = reconciliationMapper.getFrozenMarginList();
        AgentAccountListVO.forEach(a -> {
            frozenMarginList.forEach(b -> {
                if (a.getId().equals(b.getInstitution_order_id()) && a.getCurrency().equals(b.getOrder_currency())) {
                    if (b.getAccount_type().equals(TradeConstant.ACCOUNT_MARGIN)) {
                        //保证金
                        a.setMarginBalance(b.getAmount());
                    }
                }
            });
        });
        return AgentAccountListVO;
    }
}
