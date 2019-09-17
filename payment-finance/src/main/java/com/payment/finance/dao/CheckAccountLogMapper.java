package com.payment.finance.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.SearchAccountCheckDTO;
import com.payment.finance.entity.CheckAccountLog;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckAccountLogMapper extends BaseMapper<CheckAccountLog> {
    /**
     * ad3通道对账
     *
     * @param searchAccountCheckDTO 分页查询对账管理
     * @return
     */
    List<CheckAccountLog> pageAccountCheckLog(SearchAccountCheckDTO searchAccountCheckDTO);
}
