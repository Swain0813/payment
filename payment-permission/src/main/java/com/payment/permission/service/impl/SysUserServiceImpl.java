package com.payment.permission.service.impl;
import com.payment.common.base.BaseServiceImpl;
import com.payment.common.entity.SysUser;
import com.payment.common.vo.SysUserVO;
import com.payment.permission.dao.SysRoleMapper;
import com.payment.permission.dao.SysUserMapper;
import com.payment.permission.dto.SysRoleSecDto;
import com.payment.permission.dto.SysUserSecDto;
import com.payment.permission.entity.SysRole;
import com.payment.permission.service.SysUserService;
import com.payment.permission.vo.SysUserSecVO;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-22 14:32
 **/
@Service
@Transactional
public class SysUserServiceImpl extends BaseServiceImpl<SysUser> implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询用户
     **/
    @Override
    public PageInfo<SysUserSecVO> pageGetSysUser(SysUserSecDto sysUserSecDto) {
        List<SysUserSecVO> list = sysUserMapper.pageGetSysUser(sysUserSecDto);
        for (SysUserSecVO sysUserSecVO:list ) {
            if(sysUserSecVO.getInstitutionCode() != null){
                sysUserSecVO.setUsername(sysUserSecVO.getUsername().replace(sysUserSecVO.getInstitutionCode(),""));
            }
        }
        return new PageInfo<SysUserSecVO>(list);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/21
     * @Descripate 分页查询角色信息
     **/
    @Override
    public PageInfo<SysRole> pageGetSysRole(SysRoleSecDto sysRoleSecDto) {
        return new PageInfo<SysRole>(sysRoleMapper.pageGetSysRole(sysRoleSecDto));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 禁用/启用用户
     **/
    @Override
    public int banUser(SysUserVO sysUserVO, String userId, Boolean enabled) {
        SysUser sysUser = new SysUser();
        sysUser.setId(userId);
        sysUser.setType(sysUserVO.getType());//设置type的值
        sysUser.setUpdateTime(new Date());
        sysUser.setModifier(sysUserVO.getUsername());
        sysUser.setEnabled(enabled);
        return sysUserMapper.updateByPrimaryKeySelective(sysUser);
    }
}
