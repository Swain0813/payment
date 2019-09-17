package com.payment.clearing.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.BankCard;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 银行卡表mapper
 */
@Repository
public interface BankCardMapper extends BaseMapper<BankCard> {

    /**
     * 根据机构code，开户账号以及币种获取银行卡信息
     * @param institutionCode
     * @param accountCode
     * @param currency
     * @return
     */
    @Select("SELECT bank_account_code as bankAccountCode,account_name as accountName,bank_name as bankName,swift_code as swiftCode,iban," +
            "bank_code as bankCode,bank_currency as bankCurrency,bankcode_currency as bankCodeCurrency,intermediary_bank_code as intermediaryBankCode,intermediary_bank_name as intermediaryBankName," +
            "intermediary_bank_address as intermediaryBankAddress,intermediary_bank_account_no as intermediaryBankAccountNo,intermediary_bank_country as intermediaryBankCountry," +
            "intermediary_other_code as intermediaryOtherCode FROM bank_card WHERE institution_id = #{institutionCode} AND account_code=#{accountCode} AND bank_currency=#{currency} AND default_flag=TRUE AND enabled=TRUE")
    BankCard getBankCard(@Param("institutionCode") String institutionCode,
                         @Param("accountCode") String accountCode, @Param("currency") String currency);

}
