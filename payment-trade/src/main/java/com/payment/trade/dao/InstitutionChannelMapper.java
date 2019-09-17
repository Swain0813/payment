package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.InstitutionChannel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstitutionChannelMapper extends BaseMapper<InstitutionChannel> {

    /**
     * 根据机构产品id查询
     *
     * @param insProId 机构产品id
     * @return 机构通道集合
     */
    @Select("select channel_id from institution_channel where ins_pro_id = #{insProId} and enabled = true order by sort")
    List<String> selectByInsProId(@Param("insProId") String insProId);
}
