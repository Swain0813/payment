package com.payment.permission.service;

import com.payment.common.dto.InstitutionDTO;
import com.payment.common.vo.SysUserVO;
import com.payment.permission.dto.SysUserRoleDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface SysUserVoService extends UserDetailsService {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 密码加密
     **/
    String encryptPassword(String password);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/21
     * @Descripate 得到用户的具体信息
     **/
    SysUserVO getSysUser(String userName);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/21
     * @Descripate 机构添加用户
     **/
    int addSysUserbyInstitution(SysUserVO sysUserVO, SysUserRoleDto sysUserRoleDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 运营后台添加修改用户
     **/
    int addSysUserbyAdmin(String creator, SysUserRoleDto sysUserRoleDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 重置密码
     **/
    int resetPassword(String modifier, String userId);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 修改密码
     **/
    int updatePassword(String modifier, String userId, String oldPassword, String password);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 修改交易密码
     **/
    int updateTradePassword(String modifier, String userId, String oldPassword, String password);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/2/28
     * @Descripate 校验密码
     **/
    Boolean checkPassword(String oldPassword, String password);

    /**
     * 解密密码
     */
    String decryptPassword(String password);

    /**
     * 机构开户后发送邮件
     * @param institutionDTO
     */
    void openAccountEamin(InstitutionDTO institutionDTO);
}
