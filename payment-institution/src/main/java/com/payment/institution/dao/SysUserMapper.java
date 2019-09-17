package com.payment.institution.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.SysUser;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserMapper  extends BaseMapper<SysUser> {

    /**
     * @Author YangXu
     * @Date 2019/1/21
     * @Descripate 根据账号查询账号是否存在
     * @return
     **/
    int getCountByUserName(String userName);

    /**
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 判断机构是否存在
     * @return
     **/
    int getInstitutionCount(String institutionId);

}
