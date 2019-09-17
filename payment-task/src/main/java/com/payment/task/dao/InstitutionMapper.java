package com.payment.task.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Institution;
import org.springframework.stereotype.Repository;

/**
 * 机构Mapper
 */
@Repository
public interface InstitutionMapper extends BaseMapper<Institution> {

    /**
     * 根据机构code获取机构信息
     * @param institutionCode
     * @return
     */
    Institution selectByInstitutionCode(String institutionCode);
}
