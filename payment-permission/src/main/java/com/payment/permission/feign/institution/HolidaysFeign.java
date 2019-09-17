package com.payment.permission.feign.institution;

import com.payment.common.dto.HolidaysDTO;
import com.payment.common.entity.Holidays;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.impl.HolidaysFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @description: 节假日模块Feign端
 * @author: XuWenQi
 * @create: 2019-01-31 14:29
 **/
@FeignClient(value = "payment-institution", fallback = HolidaysFeignImpl.class)
public interface HolidaysFeign {

    /**
     * 添加节假日信息
     *
     * @param holidaysDTO 节假日输入实体
     * @return 添加条数
     */
    @PostMapping("/holidays/addHolidays")
    BaseResponse addHolidays(@RequestBody @ApiParam HolidaysDTO holidaysDTO);




    /**
     * 禁用节假日信息
     *
     * @param holidaysDTO 节假日id
     * @return 修改条数
     */
    @PostMapping("/holidays/banHolidays")
    BaseResponse banHolidays(@RequestBody @ApiParam HolidaysDTO holidaysDTO);


    /**
     * 分页多条件查询节假日信息
     *
     * @param holidaysDTO 节假日输入实体
     * @return 节假日输出实体集合
     */
    @PostMapping("/holidays/getByMultipleConditions")
    BaseResponse getByMultipleConditions(@RequestBody @ApiParam HolidaysDTO holidaysDTO);


    /**
     * 文件上传
     *
     * @param list
     * @return
     */
    @PostMapping(value = "/holidays/uploadFiles")
    BaseResponse uploadFiles(@RequestBody @ApiParam List<Holidays> list);
}
