package com.payment.permission.service.impl;
import com.payment.common.base.BaseServiceImpl;
import com.payment.common.vo.SysUserVO;
import com.payment.permission.dao.SysMenuMapper;
import com.payment.permission.entity.SysMenu;
import com.payment.permission.service.SysMenuService;
import com.payment.permission.vo.FirstMenuVO;
import com.payment.permission.vo.SecondMenuVO;
import com.payment.permission.vo.ThreeMenuVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-22 14:31
 **/
@Service
@Transactional
public class SysMenuServiceImpl extends BaseServiceImpl<SysMenu> implements SysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/22
     * @Descripate 得到用户所有权限
     **/
    @Override
    public List<FirstMenuVO> getAllMenu(String userId, Integer type) {
        List<FirstMenuVO> list = sysMenuMapper.getAllMenu(type);
        if (StringUtils.isNotBlank(userId)) {
            Set<String> set = sysMenuMapper.getUserMenu(userId);
            for (FirstMenuVO firstMenuVO : list) {
                if (set.contains(firstMenuVO.getId())) {
                    firstMenuVO.setFlag(true);
                }
                for (SecondMenuVO secondMenuVO : firstMenuVO.getSecondMenuVOS()) {
                    if (set.contains(secondMenuVO.getId())) {
                        secondMenuVO.setFlag(true);
                    }
                    for (ThreeMenuVO threeMenuVO : secondMenuVO.getThreeMenuVOS()) {
                        if (set.contains(threeMenuVO.getId())) {
                            threeMenuVO.setFlag(true);
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据角色id查询所有权限
     **/
    @Override
    public List<FirstMenuVO> getAllMeunByRoleId(String id, Integer type) {
        List<FirstMenuVO> list = sysMenuMapper.getAllMenu(type);
        if (StringUtils.isNotBlank(id)) {
            Set<String> set = sysMenuMapper.getRoleMenu(id);
            for (FirstMenuVO firstMenuVO : list) {
                if (set.contains(firstMenuVO.getId())) {
                    firstMenuVO.setFlag(true);
                }
                for (SecondMenuVO secondMenuVO : firstMenuVO.getSecondMenuVOS()) {
                    if (set.contains(secondMenuVO.getId())) {
                        secondMenuVO.setFlag(true);
                    }
                    for (ThreeMenuVO threeMenuVO : secondMenuVO.getThreeMenuVOS()) {
                        if (set.contains(threeMenuVO.getId())) {
                            threeMenuVO.setFlag(true);
                        }
                    }
                }
            }

        }
        return list;
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/22
     * @Descripate 添加权限
     **/
    @Override
    public int addMenu(SysMenu sysMenu) {
//        if (sysMenuMapper.getCountByMenuNameCode(sysMenu.getCnName())>0){
//            throw new BusinessException(EResultEnum.MENU_EXIST.getCode());
//        }
        sysMenu.setCreateTime(new Date());
        return sysMenuMapper.insert(sysMenu);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/22
     * @Descripate 更新权限信息
     **/
    @Override
    public int updateMenuById(SysMenu sysMenu) {
        sysMenu.setUpdateTime(new Date());
        return sysMenuMapper.updateByPrimaryKeySelective(sysMenu);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 启用禁用权限
     **/
    @Override
    public int banMenu(SysUserVO sysUserVO, String menuId, Boolean enabled) {
        SysMenu sysMenu = new SysMenu();
        sysMenu.setId(menuId);
        sysMenu.setType(sysUserVO.getType());//设置type的值
        sysMenu.setEnabled(enabled);
        sysMenu.setUpdateTime(new Date());
        sysMenu.setModifier(sysUserVO.getUsername());
        return sysMenuMapper.updateByPrimaryKeySelective(sysMenu);
    }
}
