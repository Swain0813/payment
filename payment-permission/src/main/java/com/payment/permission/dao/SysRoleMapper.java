package com.payment.permission.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.vo.SysRoleVO;
import com.payment.permission.dto.SysRoleSecDto;
import com.payment.permission.entity.SysRole;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SysRoleMapper extends BaseMapper<SysRole> {


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/22
     * @Descripate 查询rolecode是否已存在
     **/
    int getCountByRoleName(@Param("institutionId") String institutionId,@Param("roleName") String roleName);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/21
     * @Descripate 分页查询角色信息
     **/
    List<SysRole> pageGetSysRole(SysRoleSecDto sysRoleSecDto);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/23
     * @Descripate 得到所有角色
     **/
    List<SysRoleVO> getAllRole(@Param("institutionId") String institutionId,@Param("type") Integer type);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/23
     * @Descripate 根据用户id查询用户角色
     **/
    Set<String> getUserRole(String id);

}
