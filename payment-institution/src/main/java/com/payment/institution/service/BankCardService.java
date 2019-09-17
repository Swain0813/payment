package com.payment.institution.service;

import com.payment.common.base.BaseService;
import com.payment.common.dto.*;
import com.payment.common.entity.Bank;
import com.payment.common.entity.BankCard;
import com.payment.common.entity.BankIssuerid;
import com.payment.common.vo.BankCardVO;
import com.payment.common.vo.ExportBankVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface BankCardService extends BaseService<BankCard> {

    /**
     * @Author YangXu
     * @Date 2019/2/27
     * @Descripate 添加银行卡信息
     * @return
     **/
    int addBankCard(String name, List<BankCardDTO> bankCardDTO);

    /**
     * @Author YangXu
     * @Date 2019/2/27
     * @Descripate 修改银行卡信息
     * @return
     **/
    int updateBankCard(String name, BankCardDTO bankCardDTO);

    /**
     * @Author YangXu
     * @Date 2019/2/27
     * @Descripate 根据机构id查询银行卡
     * @return
     **/
    List<BankCard> selectBankCardByInsId(String institutionId);

    /**
     * @Author YangXu
     * @Date 2019/2/27
     * @Descripate 分页查询银行卡
     * @return
     **/
    PageInfo<BankCardVO> pageBankCard(BankCardSearchDTO bankCardSearchDTO);

    /**
     * @Author YangXu
     * @Date 2019/2/27
     * @Descripate 启用禁用银行卡
     * @return
     **/
    int  banBankCard(String name,String bankCardId , Boolean enabled);

    /**
     * @Author YangXu
     * @Date 2019/6/11
     * @Descripate  配置银行issureid对照信息
     * @return
     **/
    int addBankIssureId(String name,List<BankIssuerid> bankIssuerid);


    /**
     * @Author YangXu
     * @Date 2019/7/3
     * @Descripate 导入银行对照信息
     * @return
     *
     * @param list*/
    int importBankIssureId(List<BankIssuerid> list);

    /**
     * @Author YangXu
     * @Date 2019/6/11
     * @Descripate  配置银行信息
     * @return
     **/
    int addBank(String name, Bank bank);

    /**
     * @Author YangXu
     * @Date 2019/6/11
     * @Descripate  修改银行issureid对照信息
     * @return
     **/
    int updateBankIssureId(String name,BankIssuerid bankIssuerid);

    /**
     * @Author YangXu
     * @Date 2019/6/11
     * @Descripate  修改银行信息
     * @return
     **/
    int updateBank(String name,Bank bank);

    /**
     * @Author YangXu
     * @Date 2019/6/11
     * @Descripate  查询银行issureid对照信息
     * @return
     **/
    PageInfo<BankIssuerid> pageFindBankIssuerid(BankIssueridDTO bankIssueridDTO);

    /**
     * @Author YangXu
     * @Date 2019/6/11
     * @Descripate  查询银行信息
     * @return
     **/
    PageInfo<Bank> pageFindBank(BankDTO bank);

    /**
     * 设置默认银行卡
     * @param name
     * @param bankCardId
     * @param defaultFlag
     * @return
     */
    int  defaultBankCard(String name,String bankCardId , Boolean defaultFlag);

    /**
     * 导入银行
     *
     * @param banks
     * @return
     */
    int importBank(List<Bank> banks);

    /**
     * 导出银行
     *
     * @param exportBankDTO exportBankDTO
     * @return
     */
    List<ExportBankVO> exportBank(ExportBankDTO exportBankDTO);

    /**
     * 导出银行映射表信息
     *
     * @param bankIssueridDTO
     * @return
     */
    List<BankIssuerid> exportBankIssuerid(BankIssueridExportDTO bankIssueridDTO);
}
