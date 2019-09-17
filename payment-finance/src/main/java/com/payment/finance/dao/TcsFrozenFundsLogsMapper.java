package com.payment.finance.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.AccountSearchDTO;
import com.payment.common.entity.TcsFrozenFundsLogs;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TcsFrozenFundsLogsMapper extends BaseMapper<TcsFrozenFundsLogs> {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 查询冻结余额流水详情
     **/
    List<TcsFrozenFundsLogs> pageFrozenLogs(AccountSearchDTO accountSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 查询冻结余额流水详情
     **/
    List<TcsFrozenFundsLogs> exportFrozenLogs(AccountSearchDTO accountSearchDTO);
}
