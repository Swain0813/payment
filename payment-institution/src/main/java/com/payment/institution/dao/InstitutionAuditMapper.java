package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.InstitutionDTO;
import com.payment.common.vo.InstitutionDetailVO;
import com.payment.institution.entity.InstitutionAudit;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstitutionAuditMapper  extends BaseMapper<InstitutionAudit> {

    /**
     * 查询机构审核表的信息
     * @param institutionDTO
     * @return
     */
    List<InstitutionAudit> pageFindInstitutionAduit(InstitutionDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构Id和当前请求语言查询机构审核信息详情
     **/
    InstitutionDetailVO getInstitutionInfoAudit(@Param("institutionId") String id, @Param("language") String language);
}
