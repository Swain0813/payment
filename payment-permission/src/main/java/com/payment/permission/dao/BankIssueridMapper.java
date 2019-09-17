package com.payment.permission.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.BankIssuerid;
import org.springframework.stereotype.Repository;

@Repository
public interface BankIssueridMapper extends BaseMapper<BankIssuerid> {

    /**
     * 查询银行机构代码映射是否存在该记录
     * @param ol
     * @return
     */
    int findDuplicatesCount(BankIssuerid ol);
}
