package com.payment.finance.service;

import com.payment.common.dto.ReconOperDTO;
import com.payment.common.dto.ReconciliationDTO;
import com.payment.common.dto.ReconciliationExportDTO;
import com.payment.common.dto.SearchAvaBalDTO;
import com.payment.common.entity.Reconciliation;
import com.github.pagehelper.PageInfo;

import java.util.List;


public interface ReconciliationService {
    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/25
     * @Descripate 分页查询调账单
     **/
    PageInfo<Reconciliation> pageReconciliation(ReconciliationDTO reconciliationDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/25
     * @Descripate 资金变动操作
     **/
    String doReconciliation(String name, ReconOperDTO reconOperDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/25
     * @Descripate 资金变动审核
     **/
    String auditReconciliation(String name, String reconciliationId, boolean enabled, String remark);

    /**
     * 查看审核状态
     *
     * @param reconciliationDTO
     * @return
     */
    PageInfo<Reconciliation> pageReviewReconciliation(ReconciliationDTO reconciliationDTO);

    /**
     * 导出
     *
     * @param reconciliationDTO
     * @return
     */
    List<Reconciliation> exportReconciliation(ReconciliationExportDTO reconciliationDTO);

    /**
     * 查询可用余额
     *
     * @param searchAvaBalDTO
     * @return
     */
    String getAvailableBalance(SearchAvaBalDTO searchAvaBalDTO);
}
