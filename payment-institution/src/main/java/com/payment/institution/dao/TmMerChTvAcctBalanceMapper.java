package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.AccountSearchDTO;
import com.payment.common.dto.ClearSearchDTO;
import com.payment.common.entity.TmMerChTvAcctBalance;
import com.payment.common.vo.ClearAccountVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TmMerChTvAcctBalanceMapper extends BaseMapper<TmMerChTvAcctBalance> {

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/7/17
     * @Descripate 查询清算户余额流水详情
     **/
    List<ClearAccountVO> pageClearBalanceLogs(ClearSearchDTO clearSearchDTO);

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/7/17
     * @Descripate 导出清算户余额流水详情
     **/
    List<ClearAccountVO> exportClearBalanceLogs(ClearSearchDTO clearSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 查询账户余额流水详情
     **/
    List<TmMerChTvAcctBalance> pageAccountBalanceLogs(AccountSearchDTO accountSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 查询账户余额流水详情
     **/
    List<TmMerChTvAcctBalance> exportAccountBalanceLogs(AccountSearchDTO accountSearchDTO);

}
