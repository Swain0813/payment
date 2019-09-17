package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.InstitutionProductDTO;
import com.payment.common.vo.InstitutionProductVO;
import com.payment.institution.entity.InstitutionProductAudit;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstitutionProductAuditMapper  extends BaseMapper<InstitutionProductAudit> {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 分页查询机构产品审核信息
     **/
    List<InstitutionProductVO> pageFindInsProductAudit(InstitutionProductDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据产品Id查询产品审核详情
     **/
    InstitutionProductVO getInsProductAuditById(@Param("insProductId") String insProductId, @Param("language")String language);
}
