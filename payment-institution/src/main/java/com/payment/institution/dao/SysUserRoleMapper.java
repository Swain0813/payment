package com.payment.institution.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.SysUserRole;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Select("select id from sys_role where role_name like '%机构管理员%'")
    String getInstitutionRoleId();

    @Select("select id from sys_role where role_name like '%POS机管理员%'")
    String getPOSRoleId();

    @Select("select id from sys_role where role_name like '%代理商管理员%'")
    String getAgencyRoleId();
}
