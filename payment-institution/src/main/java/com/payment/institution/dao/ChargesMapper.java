package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.ChargesTypeDTO;
import com.payment.institution.entity.Charges;
import com.payment.institution.vo.ChargesTypeVO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author shen
 * 算费管理mapper
 */
@Repository
public interface ChargesMapper extends BaseMapper<Charges> {

    /**
     * 功能描述: 依据条件查询费率
     *
     * @param:
     * @return:
     * @auther: shenxinran
     * @date: 2019/1/25 11:46
     */
    List<ChargesTypeVO> pageChargesCondition(ChargesTypeDTO chargesTypeDTO);
}
