package com.payment.permission.dao;
import com.payment.common.base.BaseMapper;
import com.payment.permission.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 根据角色id删除角色权限中间表
     * @param roleId
     * @return
     */
    int deleteByRoleId(String roleId);

    /**
     * 根据权限类型查询权限id
     * @param type
     * @return
     */
    @Select("select id from sys_menu where type = #{type}")
    List<String> getMenuId(String type);

}
