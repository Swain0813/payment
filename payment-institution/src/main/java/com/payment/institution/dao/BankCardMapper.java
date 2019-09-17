package com.payment.institution.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.dto.BankCardSearchDTO;
import com.payment.common.entity.BankCard;
import com.payment.common.vo.BankCardVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankCardMapper  extends BaseMapper<BankCard> {

    /**
     * 根据机构code和银行账号获取银行卡信息
     * @param institutionId
     * @param bankAccountCode
     * @return
     */
    @Select("SELECT id  as bankCardId,institution_id as institutionCode,account_code as accountCode,bank_account_code as bankAccountCode,bank_currency as bankCurrency,bankcode_currency as bankCodeCurrency FROM bank_card WHERE institution_id = #{institutionId} and bank_account_code = #{bankAccountCode} and enabled=true")
    List<BankCardVO> getBankCards(@Param("institutionId") String institutionId,@Param("bankAccountCode") String bankAccountCode);

    /**
     * @Author YangXu
     * @Date 2019/2/27
     * @Descripate 分页查询银行卡
     * @return
     **/
    List<BankCardVO> pageBankCard(BankCardSearchDTO bankCardSearchDTO);

    /**
     * 根据银行卡id查询银行卡信息
     * @param bankCardId
     * @return
     */
    @Select("SELECT id,institution_id as institutionId,bank_account_code as bankAccountCode,bank_currency as bankCurrency,bankcode_currency as bankCodeCurrency,enabled FROM bank_card WHERE id=#{bankCardId}")
    BankCard getBankCard(@Param("bankCardId") String bankCardId);

    /**
     * 根据机构code，银行卡卡号，银行卡币种以及结算币种查询银行卡信息
     * @param institutionId
     * @param bankAccountCode
     * @param bankCurrency
     * @param bankCodeCurrency
     * @return
     */
    @Select("SELECT id,institution_id as institutionId,account_code as accountCode,bank_account_code as bankAccountCode,bank_currency as bankCurrency,bankcode_currency as bankCodeCurrency FROM bank_card WHERE institution_id = #{institutionId} and bank_account_code = #{bankAccountCode} and bank_currency = #{bankCurrency} " +
            "and bankcode_currency = #{bankCodeCurrency} and enabled=true")
    BankCard checkBankCard(@Param("institutionId") String institutionId,@Param("bankAccountCode") String bankAccountCode,
                           @Param("bankCurrency") String bankCurrency,@Param("bankCodeCurrency") String bankCodeCurrency);

    /**
     * 根据机构code，银行卡币种以及结算币种和是否设为默认银行卡查询银行卡信息
     * @param institutionId 机构id
     * @param bankCurrency 结算币种
     * @return
     */
    @Select("SELECT id,institution_id as institutionId,bank_currency as bankCurrency,bankcode_currency as bankCodeCurrency,bank_account_code as bankAccountCode from bank_card WHERE institution_id = #{institutionId} and bank_currency = #{bankCurrency} and default_flag=true")
    List<BankCard> selectUpdateBankCard(@Param("institutionId") String institutionId,@Param("bankCurrency") String bankCurrency);


    @Select("SELECT id,institution_id as institutionId,account_code as accountCode,bank_account_code as bankAccountCode,bank_currency as bankCurrency,bankcode_currency as bankCodeCurrency FROM bank_card WHERE institution_id = #{institutionId}  and bank_currency = #{bankCurrency} and default_flag=true")
    List<BankCard> checkDefaultBankCard(@Param("institutionId") String institutionId,@Param("bankCurrency") String bankCurrency);

}
