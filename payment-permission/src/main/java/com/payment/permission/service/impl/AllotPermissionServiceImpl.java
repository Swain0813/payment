package com.payment.permission.service.impl;
import com.payment.common.entity.SysUserRole;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.IDS;
import com.payment.common.vo.SysUserVO;
import com.payment.permission.dao.SysRoleMenuMapper;
import com.payment.permission.dao.SysUserMenuMapper;
import com.payment.permission.dao.SysUserRoleMapper;
import com.payment.permission.dto.SysRoleMenuDto;
import com.payment.permission.dto.SysUserMenuDto;
import com.payment.permission.dto.SysUserRoleDto;
import com.payment.permission.entity.SysRoleMenu;
import com.payment.permission.entity.SysUserMenu;
import com.payment.permission.service.AllotPermissionService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-23 14:58
 **/
@Service
@Transactional
public class AllotPermissionServiceImpl implements AllotPermissionService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysUserMenuMapper sysUserMenuMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/23
     * @Descripate 给用户分配角色
     **/
    @Override
    public int addUserRole(SysUserVO sysUserVO, SysUserRoleDto sysUserRoleDto) {
        if (sysUserRoleMapper.deleteByUserId(sysUserRoleDto.getUserId()) < 0) {
            throw new BusinessException(EResultEnum.REQUEST_REMOTE_ERROR.getCode());
        }
        List<SysUserRole> list = Lists.newArrayList();
        for (String s : sysUserRoleDto.getRoleId()) {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setId(IDS.uuid());
            sysUserRole.setCreateTime(new Date());
            sysUserRole.setRoleId(s);
            sysUserRole.setUserId(sysUserRoleDto.getUserId());
            sysUserRole.setModifier(sysUserVO.getUsername());
            list.add(sysUserRole);
        }
        return sysUserRoleMapper.insertList(list);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/23
     * @Descripate 给角色分配权限
     **/
    @Override
    public int addRoleMenu(SysUserVO sysUserVO, SysRoleMenuDto sysRoleMenuDto) {
        if (sysRoleMenuMapper.deleteByRoleId(sysRoleMenuDto.getRoleId()) < 0) {
            throw new BusinessException(EResultEnum.REQUEST_REMOTE_ERROR.getCode());
        }
        List<SysRoleMenu> list = Lists.newArrayList();
        for (String s : sysRoleMenuDto.getMenuId()) {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setId(IDS.uuid());
            sysRoleMenu.setCreateTime(new Date());
            sysRoleMenu.setUpdateTime(new Date());
            sysRoleMenu.setMenuId(s);
            sysRoleMenu.setRoleId(sysRoleMenuDto.getRoleId());
            sysRoleMenu.setModifier(sysUserVO.getUsername());
            sysRoleMenu.setCreator(sysUserVO.getUsername());
            list.add(sysRoleMenu);
        }
        //如果角色权限为空的场合
        if (list.size() == 0) {
            throw new BusinessException(EResultEnum.ROLE_PERMISSION_IS_NOT_NULL.getCode());//角色权限不能为空
        }
        return sysRoleMenuMapper.insertList(list);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/23
     * @Descripate 设置admin
     **/
    @Override
    public int setAdmin(String roldId, String type) {
        if (sysRoleMenuMapper.deleteByRoleId(roldId) < 0) {
            throw new BusinessException(EResultEnum.REQUEST_REMOTE_ERROR.getCode());
        }
        List<SysRoleMenu> list = Lists.newArrayList();
        List<String> menus = sysRoleMenuMapper.getMenuId(type);
        if(menus==null || menus.size()==0){
            throw new BusinessException(EResultEnum.ROLE_PERMISSION_IS_NOT_NULL.getCode());
        }
        for (String s : menus) {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setId(IDS.uuid());
            sysRoleMenu.setCreateTime(new Date());
            sysRoleMenu.setMenuId(s);
            sysRoleMenu.setRoleId(roldId);
            list.add(sysRoleMenu);
        }
        return sysRoleMenuMapper.insertList(list);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/23
     * @Descripate 给用户分配权限
     **/
    @Override
    public int addUserMenu(SysUserVO sysUserVO, SysUserMenuDto sysUserMenuDto) {
        if (sysUserMenuMapper.deleteByUserId(sysUserMenuDto.getUserId()) < 0) {
            throw new BusinessException(EResultEnum.REQUEST_REMOTE_ERROR.getCode());
        }
        List<SysUserMenu> list = Lists.newArrayList();
        for (String s : sysUserMenuDto.getMenuId()) {
            SysUserMenu sysUserMenu = new SysUserMenu();
            sysUserMenu.setId(IDS.uuid());
            sysUserMenu.setCreateTime(new Date());
            sysUserMenu.setMenuId(s);
            sysUserMenu.setUserId(sysUserMenuDto.getUserId());
            sysUserMenu.setModifier(sysUserVO.getUsername());
            list.add(sysUserMenu);
        }
        return sysUserMenuMapper.insertList(list);
    }
}
