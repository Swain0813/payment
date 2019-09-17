package com.payment.permission.service.impl;
import com.payment.common.base.BaseServiceImpl;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.IDS;
import com.payment.common.vo.SysRoleVO;
import com.payment.common.vo.SysUserVO;
import com.payment.permission.dao.SysRoleMapper;
import com.payment.permission.dao.SysRoleMenuMapper;
import com.payment.permission.dto.SysRoleMenuDto;
import com.payment.permission.entity.SysRole;
import com.payment.permission.entity.SysRoleMenu;
import com.payment.permission.service.SysRoleService;
import com.google.common.collect.Lists;
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
 * @create: 2019-01-22 14:34
 **/
@Service
@Transactional
public class SysRoleServiceImpl extends BaseServiceImpl<SysRole> implements SysRoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/22
     * @Descripate 添加角色
     **/
    @Override
    public int addSysRole(String createor, SysRoleMenuDto sysRoleMenuDto) {
        //添加角色
        SysRole sysRole = new SysRole();
        String roleId = IDS.uuid2();
        sysRole.setId(roleId);
        sysRole.setInstitutionId(sysRoleMenuDto.getInstitutionId());
        sysRole.setType(sysRoleMenuDto.getType());
        sysRole.setRoleCode(sysRoleMenuDto.getRoleCode());
        sysRole.setRoleName(sysRoleMenuDto.getRoleName());
        if (sysRoleMapper.getCountByRoleName(sysRoleMenuDto.getInstitutionId(),sysRole.getRoleName()) > 0) {
            throw new BusinessException(EResultEnum.ROLE_EXIST.getCode());
        }
        sysRole.setDescription(sysRoleMenuDto.getDescription());
        sysRole.setCreateTime(new Date());
        sysRole.setUpdateTime(new Date());
        sysRole.setCreator(createor);
        if (StringUtils.isBlank(sysRole.getSort().toString())) {
            sysRole.setSort(0);
        }
        sysRoleMenuDto.setRoleId(roleId);

        //添加角色信息
        if (sysRoleMenuMapper.deleteByRoleId(sysRoleMenuDto.getRoleId()) < 0) {
            throw new BusinessException(EResultEnum.REQUEST_REMOTE_ERROR.getCode());
        }
        List<SysRoleMenu> list = Lists.newArrayList();
        for (String s : sysRoleMenuDto.getMenuId()) {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setId(IDS.uuid());
            sysRoleMenu.setCreateTime(new Date());
            sysRoleMenu.setMenuId(s);
            sysRoleMenu.setRoleId(sysRoleMenuDto.getRoleId());
            sysRoleMenu.setCreator(createor);
            list.add(sysRoleMenu);
        }
        sysRoleMapper.insert(sysRole);
        return sysRoleMenuMapper.insertList(list);
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/23
     * @Descripate 查询所有角色
     **/
    @Override
    public List<SysRoleVO> getAllRole(String userId,String institutionId,Integer type) {
        List<SysRoleVO> list = sysRoleMapper.getAllRole(institutionId,type);
        if (StringUtils.isNotBlank(userId)) {
            Set<String> set = sysRoleMapper.getUserRole(userId);
            for (SysRoleVO sysRoleVO : list) {
                if (set.contains(sysRoleVO.getId())) {
                    sysRoleVO.setFlag(true);
                }
            }
        }
        return list;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 禁用启用角色
     **/
    @Override
    public int banRole(SysUserVO sysUserVO, String roleId, Boolean enabled,Integer type) {
        SysRole sysRole = new SysRole();
        sysRole.setId(roleId);
        sysRole.setType(type);//设置type的值
        sysRole.setEnabled(enabled);
        sysRole.setModifier(sysUserVO.getUsername());
        sysRole.setUpdateTime(new Date());
        return sysRoleMapper.updateByPrimaryKeySelective(sysRole);
    }
}
