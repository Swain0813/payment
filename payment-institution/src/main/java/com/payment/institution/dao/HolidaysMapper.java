package com.payment.institution.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.dto.HolidaysDTO;
import com.payment.common.entity.Holidays;
import com.payment.common.vo.HolidaysVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface HolidaysMapper extends BaseMapper<Holidays> {
    /**
     * 分页多条件查询所有节假日信息
     *
     * @param holidaysDTO 节假日输入实体
     * @return 节假日实体集合
     */
    List<HolidaysVO> pageMultipleConditions(HolidaysDTO holidaysDTO);


    /**
     * 根据国家和日期查询节假日信息
     *
     * @param country 节假日输入实体
     * @param date    日期
     * @return 节假日信息条数
     */
    int selectByCountryAndDate(@Param("country") String country, @Param("date") Date date);
}
