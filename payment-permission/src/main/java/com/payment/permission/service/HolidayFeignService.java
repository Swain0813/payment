package com.payment.permission.service;

import com.payment.common.entity.Holidays;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author shenxinran
 * @Date: 2019/2/28 14:00
 * @Description: 节假日
 */
public interface HolidayFeignService {
    List<Holidays> uploadFiles(MultipartFile file, String name);
}
