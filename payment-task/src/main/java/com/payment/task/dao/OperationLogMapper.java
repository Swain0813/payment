package com.payment.task.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.OperationLog;
import org.apache.ibatis.annotations.Delete;
import org.springframework.stereotype.Repository;


@Repository
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    /**
     * 删除不是当日的操作日志
     * @return
     */
    @Delete("DELETE FROM operation_log WHERE date(create_time)!=curdate()")
    int deleteOperatLog();
}
