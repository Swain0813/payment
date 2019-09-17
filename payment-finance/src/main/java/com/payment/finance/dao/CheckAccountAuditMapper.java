package com.payment.finance.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.SearchAccountCheckDTO;
import com.payment.finance.entity.CheckAccountAudit;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckAccountAuditMapper  extends BaseMapper<CheckAccountAudit> {


    /**
     * ad3通道对账
     *
     * @param searchAccountCheckDTO 分页查询对账管理复核详情
     * @return
     */
    List<CheckAccountAudit> pageAccountCheckAudit(SearchAccountCheckDTO searchAccountCheckDTO);

    /**
     * ad3通道对账
     *
     * @param searchAccountCheckDTO 导出对账管理复核详情
     * @return
     */
    List<CheckAccountAudit> exportAccountCheckAudit(SearchAccountCheckDTO searchAccountCheckDTO);

}
