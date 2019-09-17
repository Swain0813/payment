package com.payment.permission.dao;
import com.payment.common.base.BaseMapper;
import com.payment.permission.entity.SysUserMenu;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserMenuMapper extends BaseMapper<SysUserMenu> {
    /**
     * 根据用户id删除用户权限中间表
     * @param userId
     * @return
     */
    int deleteByUserId(String userId);
}
