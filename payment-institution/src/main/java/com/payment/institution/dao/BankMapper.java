package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.BankDTO;
import com.payment.common.dto.ExportBankDTO;
import com.payment.common.entity.Bank;
import com.payment.common.vo.ExportBankVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankMapper extends BaseMapper<Bank> {

    /**
     * 获取银行信息
     * @param bank
     * @return
     */
    List<Bank> pageFindBank(BankDTO bank);

    /**
     * 依据银行名与币种校验
     *
     * @param bank
     * @return
     */
    int selectByBankNameAndCurrency(Bank bank);

    /**
     * 导出银行信息
     *
     * @param exportBankDTO exportBankDTO
     * @return
     */
    List<ExportBankVO> exportBank(ExportBankDTO exportBankDTO);
}
