package com.payment.permission.service.impl;
import com.payment.common.base.BaseServiceImpl;
import com.payment.common.dto.OperationLogDTO;
import com.payment.common.entity.OperationLog;
import com.payment.common.utils.IDS;
import com.payment.permission.dao.OperationLogMapper;
import com.payment.permission.service.OperationLogService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

/**
 * 操作日志模块的实现类
 */
@Service
@Transactional
public class OperationLogServiceImpl extends BaseServiceImpl<OperationLog> implements OperationLogService {
    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 添加操作日志
     *
     * @param operationLogDTO
     * @return
     */
    @Override
    public int addOperationLog(OperationLogDTO operationLogDTO) {
        //创建操作日志对象
        OperationLog operationLog = new OperationLog();
        BeanUtils.copyProperties(operationLogDTO, operationLog);
        operationLog.setId(IDS.uuid2());//id
        operationLog.setCreateTime(new Date());//创建时间
        return operationLogMapper.insert(operationLog);
    }

    /**
     * 查询所有的操作日志
     *
     * @param operationLogDTO
     * @return
     */
    @Override
    public PageInfo<OperationLog> pageOperationLog(OperationLogDTO operationLogDTO) {
        return new PageInfo(operationLogMapper.pageOperLog(operationLogDTO));
    }
}
