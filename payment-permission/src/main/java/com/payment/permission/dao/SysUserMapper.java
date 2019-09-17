package com.payment.permission.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.SysUser;
import com.payment.common.vo.SysUserVO;
import com.payment.permission.dto.SysUserSecDto;
import com.payment.permission.vo.SysUserSecVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名获取用户信息
     * @param userName
     * @return
     */
    SysUserVO getSysUser(String userName);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/21
     * @Descripate 根据账号查询账号是否存在
     **/
    SysUser getSysUserByUserName(String userName);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 根据机构id查询机构号
     **/
    @Select("select institution_code from institution where id = #{institutionId}")
    String getInstitutionCodeByInstitutionId(@Param("institutionId") String institutionId);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询用户
     **/
    List<SysUserSecVO> pageGetSysUser(SysUserSecDto sysUserSecDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/1
     * @Descripate 查询交易密码
     **/
    @Select("select trade_password from sys_user where id = #{userId}")
    String getTradPassword(@Param("userId") String userId);

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/4/18
     * @Descripate 查询用户密码
     **/
    @Select("select password from sys_user where id = #{userId}")
    String getUserPassword(@Param("userId") String userId);

}
