package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.AttestationDTO;
import com.payment.common.entity.Attestation;
import com.payment.common.vo.AttestationVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttestationMapper extends BaseMapper<Attestation> {

    /**
     * @param attestationDTO
     * @return
     */
    List<AttestationVO> selectKeyInfo(AttestationDTO attestationDTO);

    /**
     * 供平台使用的方法，可以查询出平台的私钥
     *
     * @param attestationDTO
     * @return
     */
    List<Attestation> pageAllKeyInfo(AttestationDTO attestationDTO);

    /**
     * 查询公钥
     *
     * @return
     * @param institutionCode
     */
    AttestationVO selectPlatformPub(String institutionCode);

    /**
     * 通过机构code查询密钥
     *
     * @param institutionCode
     * @return
     */
    int selectByInstitutionCode(@Param("institutionCode") String institutionCode);

    /**
     * 通过公钥查询个数
     *
     * @param pubKey
     * @return
     */
    int selectByPubKey(@Param("pubKey") String pubKey);

}
