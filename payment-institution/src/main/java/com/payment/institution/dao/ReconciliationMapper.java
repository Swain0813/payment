package com.payment.institution.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.dto.FrozenMarginInfoDTO;
import com.payment.common.entity.Reconciliation;
import com.payment.common.vo.FrozenMarginInfoVO;
import com.payment.common.vo.FrozenMarginListVO;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReconciliationMapper extends BaseMapper<Reconciliation> {


    List<FrozenMarginListVO> getFrozenMarginList();

    List<FrozenMarginInfoVO> pageFrozenLogs(FrozenMarginInfoDTO frozenMarginInfoDTO);
}
