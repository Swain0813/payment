package com.payment.institution.service;

import com.payment.common.dto.HolidaysDTO;
import com.payment.common.entity.Holidays;
import com.payment.common.vo.HolidaysVO;
import com.github.pagehelper.PageInfo;

import java.util.List;


/**
 * @Author XuWenQi
 * @Date 2019/1/30 17:04
 * @Descripate 节假日接口
 */
public interface HolidaysService {

    /**
     * 添加节假日信息
     *
     * @param holidaysDTO 节假日输入实体
     * @param name        添加者姓名
     * @return 添加条数
     */
     int addHolidays(HolidaysDTO holidaysDTO, String name);


    /**
     * 修改节假日信息
     * @param holidaysDTO 节假日输入实体
     * @param name
     * @return 修改条数
     */
    int banHolidays(HolidaysDTO holidaysDTO, String name);

    /**
     * 分页多条件查询节假日信息
     *
     * @param holidaysDTO 节假日输入实体
     * @return 节假日输出实体集合
     */
    PageInfo<HolidaysVO> getByMultipleConditions(HolidaysDTO holidaysDTO);


    /**
     * 上传文件
     *
     * @param fileList
     * @return
     */
    int uploadFiles(List<Holidays> fileList);
}
