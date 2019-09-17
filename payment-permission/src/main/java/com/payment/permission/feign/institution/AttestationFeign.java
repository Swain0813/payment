package com.payment.permission.feign.institution;

import com.payment.common.dto.AttestationDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.impl.AttestationFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 密钥管理Feign接口
 */
@FeignClient(value = "payment-institution", fallback = AttestationFeignImpl.class)
public interface AttestationFeign {
    /**
     * 生成RSA公私钥
     *
     * @return
     */
    @GetMapping("/attestation/getRSA")
    BaseResponse getRSA();

    /**
     * 添加密钥
     *
     * @param attestationDTO
     * @return
     */
    @PostMapping("/attestation/addKey")
    BaseResponse addKey(@RequestBody @ApiParam AttestationDTO attestationDTO);

    /**
     * 查询商户密钥列表
     *
     * @param attestationDTO
     * @return
     */
    @PostMapping("/attestation/pageKeyInfo")
    BaseResponse pageKeyInfo(@RequestBody @ApiParam AttestationDTO attestationDTO);

    /**
     * 查询所有的密钥，包括平台的私钥
     *
     * @param attestationDTO
     * @return
     */
    @PostMapping("/attestation/pageAllKeyInfo")
    BaseResponse pageAllKeyInfo(@RequestBody @ApiParam AttestationDTO attestationDTO);

    /**
     * 更新密钥
     *
     * @param attestationDTO
     * @return
     */
    @PostMapping("/attestation/updateKeyInfo")
    BaseResponse updateKeyInfo(@RequestBody @ApiParam AttestationDTO attestationDTO);

    /**
     * 启用禁用密钥
     *
     * @param attestationDTO
     * @return
     */
    @PostMapping("/attestation/banKeyInfo")
    BaseResponse banKeyInfo(@RequestBody @ApiParam AttestationDTO attestationDTO);
}
