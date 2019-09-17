package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.InstitutionDTO;
import com.payment.common.vo.InstitutionDetailVO;
import com.payment.institution.entity.InstitutionHistory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface InstitutionHistoryMapper   extends BaseMapper<InstitutionHistory> {

    /**
     * @Author YangXu
     * @Date 2019/3/1
     * @Descripate 分页查询机构历史记录信息列表
     * @return
     **/
    List<InstitutionHistory> pageFindInstitutionHistory(InstitutionDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/1
     * @Descripate 根据机构Id查询机构变更信息详情
     **/
    InstitutionDetailVO getInstitutionHistoryInfo(String id);
}
