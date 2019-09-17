package com.payment.clearing.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Institution;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitutionMapper extends BaseMapper<Institution> {


    /**
     * @Author YangXu
     * @Date 2019/7/25
     * @Descripate 根据机构code查询机构信息
     * @return
     **/
    Institution selectByInstitutionCode(@Param("institutionCode") String institutionCode);
}
