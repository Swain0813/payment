package com.payment.institution.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.dto.AccountSearchDTO;
import com.payment.common.entity.TcsStFlow;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TcsStFlowMapper extends BaseMapper<TcsStFlow> {

    /**
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 查询结算户余额流水详情
     * @return
     **/
    List<TcsStFlow> pageSettleLogs(AccountSearchDTO accountSearchDTO);
}
