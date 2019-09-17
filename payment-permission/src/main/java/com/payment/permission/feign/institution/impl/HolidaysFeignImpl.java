package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.HolidaysDTO;
import com.payment.common.entity.Holidays;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.institution.HolidaysFeign;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description: 节假日Feign端熔断降级实现类
 * @author: XuWenQi
 * @create: 2019-01-31 14:31
 **/

@Component
public class HolidaysFeignImpl implements HolidaysFeign {
    @Override
    public BaseResponse addHolidays(HolidaysDTO holidaysDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse banHolidays(HolidaysDTO holidaysDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }


    @Override
    public BaseResponse getByMultipleConditions(HolidaysDTO holidaysDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }


    @Override
    public BaseResponse uploadFiles(List<Holidays> list) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

}
