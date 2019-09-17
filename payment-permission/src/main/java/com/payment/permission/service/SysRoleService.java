package com.payment.permission.service;

import com.payment.common.base.BaseService;
import com.payment.common.vo.SysRoleVO;
import com.payment.common.vo.SysUserVO;
import com.payment.permission.dto.SysRoleMenuDto;
import com.payment.permission.entity.SysRole;

import java.util.List;


public interface SysRoleService extends BaseService<SysRole> {
    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/22
     * @Descripate 添加角色
     **/
    int addSysRole(String createor, SysRoleMenuDto sysRoleMenuDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/23
     * @Descripate 得到所有角色
     **/
    List<SysRoleVO> getAllRole(String userId ,String institutionId,Integer type);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 禁用启用角色
     **/
    int banRole(SysUserVO sysUserVO, String roleId, Boolean enabled,Integer type);

}
