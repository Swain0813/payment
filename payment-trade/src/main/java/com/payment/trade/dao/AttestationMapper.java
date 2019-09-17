package com.payment.trade.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Attestation;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttestationMapper extends BaseMapper<Attestation> {
    /**
     * 查询 institutionCode 机构code 对应的签名信息
     *
     * @param institutionCode
     * @return
     */
    Attestation selectByInstitutionCode(@Param("institutionCode") String institutionCode);

    /**
     * 查询平台公私钥信息
     */
    Attestation selectPlatformInfo();
}
