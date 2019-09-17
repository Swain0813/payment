package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据机构code与设备操作员查询
     *
     * @param username        用户名
     * @return 关联实体
     */
    SysUser selectByInstitutionCodeAndUserName(@Param("username") String username);
}
