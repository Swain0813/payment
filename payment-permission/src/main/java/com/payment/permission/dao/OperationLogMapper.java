package com.payment.permission.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.OperationLogDTO;
import com.payment.common.entity.OperationLog;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    /**
     * 查询所有操作日志
     *
     * @param operationLogDTO
     * @return
     */
    List<OperationLog> pageOperLog(OperationLogDTO operationLogDTO);
}
