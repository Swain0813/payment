package com.payment.permission.service;

import com.payment.common.vo.SysUserVO;
import com.payment.permission.dto.SysRoleMenuDto;
import com.payment.permission.dto.SysUserMenuDto;
import com.payment.permission.dto.SysUserRoleDto;

public interface AllotPermissionService {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/23
     * @Descripate 给用户分配角色
     **/
    int addUserRole(SysUserVO sysUserVO, SysUserRoleDto sysUserRoleDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/23
     * @Descripate 给角色分配权限
     **/
    int addRoleMenu(SysUserVO sysUserVO, SysRoleMenuDto sysRoleMenuDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/23
     * @Descripate 设置admin
     **/
    int setAdmin(String roldId,String type);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/23
     * @Descripate 给用户分配权限
     **/
    int addUserMenu(SysUserVO sysUserVO, SysUserMenuDto sysUserMenuDto);
}
