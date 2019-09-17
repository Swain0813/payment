package com.payment.permission.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.entity.Holidays;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.IDS;
import com.payment.permission.dao.HolidaysMapper;
import com.payment.permission.service.HolidayFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author shenxinran
 * @Date: 2019/2/28 14:00
 * @Description:
 */
@Service
public class HolidayFeignServiceImpl implements HolidayFeignService {

    @Autowired
    private HolidaysMapper holidaysMapper;

    @Override
    public List<Holidays> uploadFiles(MultipartFile file, String name) {
        ArrayList<Holidays> h = new ArrayList<>();
        String fileName = file.getOriginalFilename();
        // 判断格式0
        if (!fileName.matches("^.+\\.(?i)(xls)$") && !fileName.matches("^.+\\.(?i)(xlsx)$")) {
            throw new BusinessException(EResultEnum.FILE_FORMAT_ERROR.getCode());
        }
        ExcelReader reader;
        try {
            reader = ExcelUtil.getReader(file.getInputStream());
        } catch (Exception e) {
            // 当excel内的格式不正确时
            throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
        }

        List<List<Object>> read = reader.read();
        //判断是否超过上传限制
        if (read.size() - 1 > AsianWalletConstant.UPLOAD_LIMIT) {
            throw new BusinessException(EResultEnum.EXCEEDING_UPLOAD_LIMIT.getCode());
        }
        if (read.size() <= 0) {
            throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
        }
        for (int i = 1; i < read.size(); i++) {
            List<Object> objects = read.get(i);
            Holidays holidays = new Holidays();
            //判断传入的excel的格式是否符合约定
            if (StringUtils.isEmpty(objects.get(0))
                    || StringUtils.isEmpty(objects.get(1))
                    || objects.size() != 3
                    || StringUtils.isEmpty(objects.get(2))
            ) {
                throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
            }
            //判断添加的时间是否过期
            DateTime parse = new DateTime();
            try {
                holidays.setCountry(objects.get(0).toString().replaceAll("\\s*", ""));
                holidays.setName(objects.get(2).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
                parse = DateUtil.parse(objects.get(1).toString().replaceAll("\\s*", ""), "yyyy-MM-dd");
            } catch (Exception e) {
                // 当excel内的格式不正确时
                throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
            }
            if (compareTime(parse, new Date())) {
                continue;
            }
            holidays.setDate(parse);
            holidays.setId(IDS.uuid2());
            holidays.setEnabled(true);
            holidays.setCreator(name);
            holidays.setCreateTime(new Date());
            //判断节日是否重复
            if (holidaysMapper.findDuplicatesCount(holidays) > 0) {
                continue;
            }
            h.add(holidays);
        }
        if (h.size() == 0) {
            throw new BusinessException(EResultEnum.IMPORT_REPEAT_ERROR.getCode());
        }
        return h;
    }

    /**
     * 比较两个时间的前后, time1 > time2 ，false,反之为true
     *
     * @param time1
     * @param time2
     * @return true or false
     */
    private boolean compareTime(Date time1, Date time2) {
        if (time1.getTime() > time2.getTime()) {
            return false;
        }
        return true;
    }
}
