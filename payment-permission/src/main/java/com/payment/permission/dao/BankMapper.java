package com.payment.permission.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Bank;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankMapper extends BaseMapper<Bank> {

    /**
     * 查询所有的银行名称
     * @return
     */
    List<String> selectAllBankName();

    /**
     * 查询银行表中是否存在该记录
     * @param ol
     * @return
     */
    int findDuplicatesCount(Bank ol);
}
