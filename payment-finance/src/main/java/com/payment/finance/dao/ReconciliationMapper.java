package com.payment.finance.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.ReconciliationDTO;
import com.payment.common.dto.ReconciliationExportDTO;
import com.payment.common.dto.SearchAvaBalDTO;
import com.payment.common.entity.Reconciliation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReconciliationMapper extends BaseMapper<Reconciliation> {

    /**
     * @Author YangXu
     * @Date 2019/3/26
     * @Descripate 根据id修改调账单状态
     * @return
     **/
    @Update("update reconciliation set status = #{status} ,remark1 = #{remark1},update_time=NOW(),modifier=#{name} where id = #{id}")
    int updateStatusById(@Param("id") String id, @Param("status") int status, @Param("name") String name, @Param("remark1") String remark1);

    /**
     * @Author YangXu
     * @Date 2019/3/25
     * @Descripate  分页查询调账单
     * @return
     **/
    List<Reconciliation> pageReconciliation(ReconciliationDTO reconciliationDTO);



    /**
     * 查看审核
     *
     * @param reconciliationDTO
     * @return
     */
    List<Reconciliation> pageReviewReconciliation(ReconciliationDTO reconciliationDTO);

    /**
     * 导出
     *
     * @param reconciliationDTO
     * @return
     */
    List<Reconciliation> pageExportReconciliation(ReconciliationExportDTO reconciliationDTO);


    /**
     * 查询可用余额
     *
     * @param searchAvaBalDTO
     * @return
     */
    BigDecimal selectAvailableBalance(SearchAvaBalDTO searchAvaBalDTO);

    /**
     * 查询冻结金额
     *
     * @param searchAvaBalDTO
     * @return
     */
    BigDecimal selectFreezeBalance(SearchAvaBalDTO searchAvaBalDTO);
}
