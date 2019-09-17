package com.payment.permission.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Attestation;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface AttestationMapper extends BaseMapper<Attestation> {

    /**
     * 查询公钥
     *
     * @return
     */
    @Select("select * from attestation where type = 3 and enabled = true")
    Attestation selectPlatformPub();


}
