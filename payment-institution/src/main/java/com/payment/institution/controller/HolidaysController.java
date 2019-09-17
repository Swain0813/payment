package com.payment.institution.controller;

import com.payment.common.base.BaseController;
import com.payment.common.dto.HolidaysDTO;
import com.payment.common.entity.Holidays;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.institution.service.HolidaysService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description: 节假日接口
 * @author: XuWenQi
 * @create: 2019-01-30 16:31
 **/
@RestController
@Api(description = "节假日接口")
@RequestMapping("/holidays")
public class HolidaysController extends BaseController {

    @Autowired
    private HolidaysService holidaysService;

    @ApiOperation(value = "添加节假日信息")
    @PostMapping("addHolidays")
    public BaseResponse addHolidays(@RequestBody @ApiParam HolidaysDTO holidaysDTO) {
        return ResultUtil.success(holidaysService.addHolidays(holidaysDTO, this.getSysUserVO().getUsername()));
    }

    @ApiOperation(value = "禁用节假日信息")
    @PostMapping("banHolidays")
    public BaseResponse updateHolidays(@RequestBody @ApiParam HolidaysDTO holidaysDTO) {
        return ResultUtil.success(holidaysService.banHolidays(holidaysDTO, this.getSysUserVO().getUsername()));
    }

    @ApiOperation(value = "分页多条件查询节假日信息")
    @PostMapping("getByMultipleConditions")
    public BaseResponse getByMultipleConditions(@RequestBody @ApiParam HolidaysDTO holidaysDTO) {
        return ResultUtil.success(holidaysService.getByMultipleConditions(holidaysDTO));
    }

    @ApiOperation(value = "导入节假日信息")
    @PostMapping("uploadFiles")
    public BaseResponse uploadFiles(@RequestBody @ApiParam List<Holidays> fileList) {
        return ResultUtil.success(holidaysService.uploadFiles(fileList));
    }
}


