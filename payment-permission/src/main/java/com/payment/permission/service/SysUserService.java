package com.payment.permission.service;
import com.payment.common.base.BaseService;
import com.payment.common.entity.SysUser;
import com.payment.common.vo.SysUserVO;
import com.payment.permission.dto.SysRoleSecDto;
import com.payment.permission.dto.SysUserSecDto;
import com.payment.permission.entity.SysRole;
import com.payment.permission.vo.SysUserSecVO;
import com.github.pagehelper.PageInfo;

public interface SysUserService extends BaseService<SysUser> {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/21
     * @Descripate 分页查询用户
     **/
    PageInfo<SysUserSecVO> pageGetSysUser(SysUserSecDto sysUserSecDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/21
     * @Descripate 分页查询角色信息
     **/
    PageInfo<SysRole> pageGetSysRole(SysRoleSecDto sysRoleSecDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 禁用/启用用户
     **/
    int banUser(SysUserVO sysUserVO, String userId, Boolean enabled);
}
