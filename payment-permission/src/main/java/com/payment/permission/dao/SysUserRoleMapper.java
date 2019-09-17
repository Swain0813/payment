package com.payment.permission.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.SysUserRole;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 根据用户id删除用户角色中间表信息
     * @param userId
     * @return
     */
    int deleteByUserId(String userId);


}
