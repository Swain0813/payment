package com.payment.permission.service;

import com.payment.common.base.BaseService;
import com.payment.common.vo.SysUserVO;
import com.payment.permission.entity.SysMenu;
import com.payment.permission.vo.FirstMenuVO;

import java.util.List;

public interface SysMenuService extends BaseService<SysMenu> {
    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/22
     * @Descripate 添加权限
     **/
    int addMenu(SysMenu sysMenu);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/22
     * @Descripate 根据Id修改
     **/
    int updateMenuById(SysMenu sysMenu);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/22
     * @Descripate 得到所有权限
     **/
    List<FirstMenuVO> getAllMenu(String userId, Integer type);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据角色id得到所有权限
     **/
    List<FirstMenuVO> getAllMeunByRoleId(String id, Integer type);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 启用禁用权限
     **/
    int banMenu(SysUserVO sysUserVO, String menuId, Boolean enabled);
}
