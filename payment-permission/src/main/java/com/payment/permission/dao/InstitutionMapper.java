package com.payment.permission.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Institution;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface InstitutionMapper extends BaseMapper<Institution> {


    /**
     * 通过code查询机构
     * @param code
     * @return
     */
    Institution selectByCode(@Param("code") String code);


}
