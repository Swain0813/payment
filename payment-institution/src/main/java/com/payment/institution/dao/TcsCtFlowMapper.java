package com.payment.institution.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.dto.AccountSearchDTO;
import com.payment.common.entity.TcsCtFlow;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TcsCtFlowMapper extends BaseMapper<TcsCtFlow> {

    /**
     * @Author YangXu
     * @Date 2019/3/12
     * @Descripate 查询清算户余额流水详情
     * @return
     **/
    List<TcsCtFlow> pagecClearLogs(AccountSearchDTO accountSearchDTO);

}
