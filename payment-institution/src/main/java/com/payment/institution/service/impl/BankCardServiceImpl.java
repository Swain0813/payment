package com.payment.institution.service.impl;

import com.payment.common.base.BaseServiceImpl;
import com.payment.common.dto.*;
import com.payment.common.entity.Bank;
import com.payment.common.entity.BankCard;
import com.payment.common.entity.BankIssuerid;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.IDS;
import com.payment.common.vo.BankCardVO;
import com.payment.common.vo.ExportBankVO;
import com.payment.institution.dao.AccountMapper;
import com.payment.institution.dao.BankCardMapper;
import com.payment.institution.dao.BankIssueridMapper;
import com.payment.institution.dao.BankMapper;
import com.payment.institution.service.BankCardService;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-02-27 17:59
 **/
@Service
@Slf4j
@Transactional
public class BankCardServiceImpl extends BaseServiceImpl<BankCard> implements BankCardService {

    @Autowired
    private BankCardMapper bankCardMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private BankIssueridMapper bankIssueridMapper;

    @Autowired
    private BankMapper bankMapper;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/2/27
     * @Descripate 添加银行卡信息
     **/
    @Override
    public int addBankCard(String name, List<BankCardDTO> list) {
        List<BankCard> bankCardList = Lists.newArrayList();
        for (int i = 0; i < list.size(); i++) {
            //根据机构code和结算币种获取账户信息
            String accountCode = accountMapper.getAccountCode(list.get(i).getInstitutionId(), list.get(i).getBankCurrency());
            if (StringUtils.isEmpty(accountCode)) {//账户信息不存在
                throw new BusinessException(EResultEnum.ACCOUNT_IS_NOT_EXIST.getCode());
            }
            //判断该机构下的银行账户下的该银行卡币种是不是已经存在
            List<BankCardVO> bankCards = bankCardMapper.getBankCards(list.get(i).getInstitutionId(), list.get(i).getBankAccountCode());
            if (bankCards != null && bankCards.size() > 0) {
                for (BankCardVO bc : bankCards) {
                    //结算币种
                    if (bc.getBankCurrency().equals(list.get(i).getBankCurrency())) {
                        //信息已存在
                        throw new BusinessException(EResultEnum.REPEATED_ADDITION.getCode());
                    }
                }
            }
            boolean flag = true;
            //不为第一条时
            if (i != 0) {
                for (int j = 0; j < list.size(); j++) {
                    //同一条时
                    if (i == j) {
                        continue;
                    }
                    if (list.get(i).getBankCurrency().equals(list.get(j).getBankCurrency())) {
                        //有相同的结算币种就不设置为默认银行卡
                        flag = false;
                        break;
                    }
                }
            }
            //根据机构code，银行卡币种以及结算币种和启用禁用状态和是否设为默认银行卡查询银行卡信息
            List<BankCard> lists = bankCardMapper.selectUpdateBankCard(list.get(i).getInstitutionId(), list.get(i).getBankCurrency());
            if (lists != null && !lists.isEmpty()) {//存在的场合
                for (BankCard bankCard : lists) {
                    bankCard.setDefaultFlag(false);
                    bankCardMapper.updateByPrimaryKeySelective(bankCard);//将存在的更新为不为默认的银行卡
                }
            }
            BankCard bankCard = new BankCard();
            BeanUtils.copyProperties(list.get(i), bankCard);
            bankCard.setAccountCode(accountCode);//账户编号
            bankCard.setId(IDS.uuid2());//id
            bankCard.setCreateTime(new Date());
            bankCard.setCreator(name);
            bankCard.setEnabled(true);
            bankCard.setDefaultFlag(flag);//设为默认银行卡
            bankCardList.add(bankCard);
        }
        return bankCardMapper.insertList(bankCardList);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/2/27
     * @Descripate 修改银行卡信息
     **/
    @Override
    public int updateBankCard(String name, BankCardDTO bankCardDTO) {
        BankCard bankCard = new BankCard();
        BeanUtils.copyProperties(bankCardDTO, bankCard);
        bankCard.setId(bankCardDTO.getBankCardId());
        bankCard.setModifier(name);
        bankCard.setUpdateTime(new Date());
        return bankCardMapper.updateByPrimaryKeySelective(bankCard);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/2/27
     * @Descripate 根据机构id查询银行卡
     **/
    @Override
    public List<BankCard> selectBankCardByInsId(String institutionId) {
        BankCard bankCard = new BankCard();
        bankCard.setInstitutionId(institutionId);
        return bankCardMapper.select(bankCard);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/2/27
     * @Descripate 分页查询银行卡
     **/
    @Override
    public PageInfo<BankCardVO> pageBankCard(BankCardSearchDTO bankCardSearchDTO) {
        return new PageInfo<BankCardVO>(bankCardMapper.pageBankCard(bankCardSearchDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/2/27
     * @Descripate 启用禁用银行卡
     **/
    @Override
    public int banBankCard(String name, String bankCardId, Boolean enabled) {
        //查询银行卡信息是不是存在
        BankCard bankCardInfo = bankCardMapper.getBankCard(bankCardId);
        if (bankCardInfo == null) {//银行卡信息不存在
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        //根据启用禁用状态判断
        if (enabled) {//启用时判断是不是已经存在
            BankCard checkbankCard = bankCardMapper.checkBankCard(bankCardInfo.getInstitutionId(), bankCardInfo.getBankAccountCode(),
                    bankCardInfo.getBankCurrency(), bankCardInfo.getBankCodeCurrency());
            if (checkbankCard != null) {
                throw new BusinessException(EResultEnum.REPEATED_ADDITION.getCode());//信息已存在
            }
        }
        BankCard bankCard = new BankCard();
        bankCard.setId(bankCardId);
        bankCard.setEnabled(enabled);
        if (!enabled) {//禁用的场合，取消默认银行卡
            bankCard.setDefaultFlag(false);
        }
        bankCard.setUpdateTime(new Date());
        bankCard.setModifier(name);
        return bankCardMapper.updateByPrimaryKeySelective(bankCard);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/6/11
     * @Descripate 配置银行信息
     **/
    @Override
    public int addBank(String name, Bank bank) {
        //非空的check
        if (StringUtils.isEmpty(bank.getBankName()) || StringUtils.isEmpty(bank.getBankCountry())
                || StringUtils.isEmpty(bank.getBankCountry()) || StringUtils.isEmpty(bank.getIssuerId())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        bank.setBankCode(IDS.uniqueID().toString());
        bank.setCreator(name);
        bank.setCreateTime(new Date());
        if (bankMapper.selectByBankNameAndCurrency(bank) > 0) {
            throw new BusinessException(EResultEnum.REPEATED_ADDITION.getCode());
        }
        return bankMapper.insert(bank);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/6/11
     * @Descripate 配置银行issureid对照信息
     **/
    @Override
    public int addBankIssureId(String name, List<BankIssuerid> bankIssuerid) {
        List<BankIssuerid> list = new ArrayList<>();
        for (BankIssuerid b : bankIssuerid) {
            if (bankIssueridMapper.getBankIssuerIdByCI(b.getChannelCode(), b.getBankName(), b.getCurrency()) > 0) {
                continue;
            }
            b.setId(IDS.uuid2());
            b.setEnabled(true);
            b.setCreator(name);
            b.setCreateTime(new Date());
            list.add(b);
        }
        if (list.size() == 0) {
            return 0;
        } else {
            return bankIssueridMapper.insertList(list);
        }
    }

    /**
     * @param list
     * @return
     * @Author YangXu
     * @Date 2019/7/3
     * @Descripate 导入银行对照信息
     */
    @Override
    public int importBankIssureId(List<BankIssuerid> list) {
        return bankIssueridMapper.insertList(list);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/6/11
     * @Descripate 配置银行issureid对照信息
     **/
    @Override
    public int updateBankIssureId(String name, BankIssuerid bankIssuerid) {
        bankIssuerid.setModifier(name);
        bankIssuerid.setEnabled(true);
        bankIssuerid.setUpdateTime(new Date());
        return bankIssueridMapper.updateByPrimaryKeySelective(bankIssuerid);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/6/11
     * @Descripate 修改银行信息
     **/
    @Override
    public int updateBank(String name, Bank bank) {
        Bank oldBank = bankMapper.selectByPrimaryKey(bank);
        if (oldBank == null) {
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        //修改银行名称时也要把issuer_id对应表的信息修改
        List<BankIssuerid> bankIssuerids = bankIssueridMapper.selectByBankName(oldBank.getBankName(), bank.getBankCurrency());
        for (BankIssuerid bankIssuerid : bankIssuerids) {
            bankIssuerid.setBankName(bank.getBankName());//银行名称
            bankIssuerid.setModifier(name);//修改人
            bankIssuerid.setUpdateTime(new Date());//修改时间
            bankIssueridMapper.updateByPrimaryKeySelective(bankIssuerid);
        }
        //修改银行信息
        bank.setModifier(name);//修改人
        bank.setUpdateTime(new Date());//更新人
        return bankMapper.updateByPrimaryKeySelective(bank);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/6/11
     * @Descripate 查询银行issureid对照信息
     **/
    @Override
    public PageInfo<BankIssuerid> pageFindBankIssuerid(BankIssueridDTO bankIssueridDTO) {
        return new PageInfo<BankIssuerid>(bankIssueridMapper.pageFindBankIssuerid(bankIssueridDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/6/11
     * @Descripate 查询银行信息
     **/
    @Override
    public PageInfo<Bank> pageFindBank(BankDTO bank) {
        return new PageInfo<Bank>(bankMapper.pageFindBank(bank));
    }

    /**
     * 设置默认银行卡
     *
     * @param name
     * @param bankCardId
     * @param defaultFlag
     * @return
     */
    @Override
    public int defaultBankCard(String name, String bankCardId, Boolean defaultFlag) {
        //查询银行卡信息是不是存在
        BankCard bankCardInfo = bankCardMapper.getBankCard(bankCardId);
        if (bankCardInfo == null) {//银行卡信息不存在
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        //已经禁用的银行卡信息不能设置为默认银行卡
        if (!bankCardInfo.getEnabled()) {
            //已经禁用的银行卡信息不能设置成默认银行卡
            throw new BusinessException(EResultEnum.ENABLED_BANK_ACCOUT_CODE_IS_ERROR.getCode());
        }
        //根据是否设为默认银行卡判断
        if (defaultFlag) {//默认银行卡是否已经存在判断是不是已经存在
            List<BankCard> bankCards = bankCardMapper.checkDefaultBankCard(bankCardInfo.getInstitutionId(), bankCardInfo.getBankCurrency());
            if (bankCards != null && !bankCards.isEmpty()) {
                //该机构相同的银行卡币种和结算币种的默认银行卡已存在
                throw new BusinessException(EResultEnum.DEFALUT_BANK_ACCOUT_CODE_IS_EXISTS.getCode());
            }
        }
        BankCard bankCard = new BankCard();
        bankCard.setId(bankCardId);
        bankCard.setDefaultFlag(defaultFlag);
        bankCard.setUpdateTime(new Date());
        bankCard.setModifier(name);
        return bankCardMapper.updateByPrimaryKeySelective(bankCard);
    }

    /**
     * 导入银行
     *
     * @param banks
     * @return
     */
    @Override
    public int importBank(List<Bank> banks) {
        return bankMapper.insertList(banks);
    }

    /**
     * 导出银行
     *
     * @param exportBankDTO exportBankDTO
     * @return ExportBankVO
     */
    @Override
    public List<ExportBankVO> exportBank(ExportBankDTO exportBankDTO) {
        return bankMapper.exportBank(exportBankDTO);
    }


    /**
     * 导出银行映射表信息
     *
     * @param bankIssueridDTO
     * @return
     */
    @Override
    public List<BankIssuerid> exportBankIssuerid(BankIssueridExportDTO bankIssueridDTO) {
        return bankIssueridMapper.exportBankIssuerid(bankIssueridDTO);
    }
}
