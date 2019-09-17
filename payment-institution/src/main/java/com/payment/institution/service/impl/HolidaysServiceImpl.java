package com.payment.institution.service.impl;
import com.payment.common.base.BaseServiceImpl;
import com.payment.common.dto.HolidaysDTO;
import com.payment.common.entity.Holidays;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.IDS;
import com.payment.common.vo.HolidaysVO;
import com.payment.institution.dao.HolidaysMapper;
import com.payment.institution.service.HolidaysService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.Date;
import java.util.List;

/**
 * @Author XuWenQi
 * @Date 2019/1/30 17:08
 * @Descripate 节假日接口实现类
 */

@Service
@Transactional
public class HolidaysServiceImpl extends BaseServiceImpl<Holidays> implements HolidaysService {

    @Autowired
    private HolidaysMapper holidaysMapper;

    /**
     * 添加节假日信息
     *
     * @param holidaysDTO 节假日输入实体
     * @param name        添加者姓名
     * @return 添加条数
     */
    @Override
    public int addHolidays(HolidaysDTO holidaysDTO, String name) {
        //非空check
        if (StringUtils.isEmpty(holidaysDTO.getCountry())) { //国家
            throw new BusinessException(EResultEnum.HOLIDAYS_COUNTRY_IS_NOT_NULL.getCode());
        }
        if (holidaysDTO.getDate() == null) { //日期
            throw new BusinessException(EResultEnum.HOLIDAYS_DATE_IS_NOT_NULL.getCode());
        }
        if (StringUtils.isEmpty(holidaysDTO.getName())) { //节假日名称
            throw new BusinessException(EResultEnum.HOLIDAYS_NAME_IS_NOT_NULL.getCode());
        }
        if (compareTime(holidaysDTO.getDate(), new Date())) {//判断添加的时间是否过期
            throw new BusinessException(EResultEnum.HOLIDAYS_ADD_TIME_EXPIRED.getCode());
        }
        //判断节假日信息是否存在
        if (holidaysMapper.selectByCountryAndDate(holidaysDTO.getCountry(), holidaysDTO.getDate()) > 0) {
            throw new BusinessException(EResultEnum.HOLIDAYS_INFO_EXIST.getCode());
        }
        Holidays holidays = new Holidays();
        BeanUtils.copyProperties(holidaysDTO, holidays);
        holidays.setId(IDS.uuid2());
        holidays.setEnabled(true);
        holidays.setCreateTime(new Date());
        holidays.setCreator(name);
        return holidaysMapper.insert(holidays);
    }

    /**
     * 禁用节假日信息
     *
     * @param holidaysDTO 节假日id
     * @param name        修改者姓名
     * @return 修改条数
     */
    @Override
    public int banHolidays(HolidaysDTO holidaysDTO, String name) {
        //非空check
        if (StringUtils.isEmpty(holidaysDTO.getId())) { //节假日id
            throw new BusinessException(EResultEnum.HOLIDAYS_ID_IS_NOT_NULL.getCode());
        }
        if (holidaysDTO.getEnabled() == null) { //节假日状态
            throw new BusinessException(EResultEnum.HOLIDAYS_ID_IS_NOT_NULL.getCode());
        }
        Holidays holidays = new Holidays();
        holidays.setId(holidaysDTO.getId());
        holidays.setEnabled(holidaysDTO.getEnabled());
        holidays.setUpdateTime(new Date());
        holidays.setModifier(name);
        return holidaysMapper.updateByPrimaryKeySelective(holidays);
    }


    /**
     * 分页多条件查询节假日信息
     *
     * @param holidaysDTO 节假日输入实体
     * @return 节假日输出实体集合
     */
    @Override
    public PageInfo<HolidaysVO> getByMultipleConditions(HolidaysDTO holidaysDTO) {
        return new PageInfo(holidaysMapper.pageMultipleConditions(holidaysDTO));
    }


    /**
     * 上传文件
     *
     * @param fileList
     * @return
     */
    @Override
    public int uploadFiles(List<Holidays> fileList) {
        return holidaysMapper.insertList(fileList);
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
