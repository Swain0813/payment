package com.payment.finance.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Institution;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface InstitutionMapper extends BaseMapper<Institution> {



    /**
     * @Author YangXu
     * @Date 2019/3/25
     * @Descripate 通过code查询机构
     * @return
     **/
    Institution selectByCode(@Param("code") String code);


}
