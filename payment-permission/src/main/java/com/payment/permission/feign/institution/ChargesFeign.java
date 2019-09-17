package com.payment.permission.feign.institution;

import com.payment.common.dto.ChargesTypeDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.impl.ChargesFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 算费管理Feign接口
 *
 * @author shen
 */
@Component
@FeignClient(value = "payment-institution", fallback = ChargesFeignImpl.class)
public interface ChargesFeign {
    /**
     * 分页查询所有算费
     *
     * @return
     */
    @PostMapping("/chargestype/pageChargesCondition")
    BaseResponse pageChargesCondition(@RequestBody @ApiParam ChargesTypeDTO chargesTypeDTO);

    /**
     * 根据据ID 查询
     *
     * @param
     * @return
     */
    @PostMapping("/chargestype/getChargesInfo")
    BaseResponse getChargesInfo(@RequestBody @ApiParam ChargesTypeDTO chargesTypeDTO);


    /**
     * 新增算费
     *
     * @param chargesTypeDTO
     * @return
     */
    @PostMapping("/chargestype/addChargesType")
    BaseResponse addChargesType(@RequestBody @ApiParam ChargesTypeDTO chargesTypeDTO);

    /**
     * 更新算费
     *
     * @param chargesTypeDTO
     * @return
     */
    @PostMapping("/chargestype/updateChargesType")
    BaseResponse updateChargesType(@RequestBody @ApiParam ChargesTypeDTO chargesTypeDTO);

    /**
     * 禁用算费
     *
     * @return
     */
    @PostMapping("/chargestype/banChargesType")
    BaseResponse banChargesType(@RequestBody @ApiParam ChargesTypeDTO chargesTypeDTO);
}
