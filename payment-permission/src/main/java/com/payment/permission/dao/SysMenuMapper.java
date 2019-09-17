package com.payment.permission.dao;
import com.payment.common.base.BaseMapper;
import com.payment.permission.entity.SysMenu;
import com.payment.permission.vo.FirstMenuVO;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/22
     * @Descripate 得到所有权限
     **/
    List<FirstMenuVO> getAllMenu(Integer type);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/23
     * @Descripate 根据用户Id查询用户权限
     **/
    Set<String> getUserMenu(String id);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据角色id得到角色所有权限
     **/
    Set<String> getRoleMenu(String id);


}
