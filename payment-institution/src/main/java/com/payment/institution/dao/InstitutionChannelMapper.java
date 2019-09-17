package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.SearchChannelDTO;
import com.payment.common.dto.SearchChannelExportDTO;
import com.payment.common.entity.InstitutionChannel;
import com.payment.common.vo.ProductChannelVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstitutionChannelMapper extends BaseMapper<InstitutionChannel> {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 分页查询产品通道管理信息
     **/
    List<ProductChannelVO> pageFindProductChannel(SearchChannelDTO searchChannelDTO);

    /**
     * 根据机构产品中间表id删除机构通道表中的信息
     *
     * @param id 机构产品中间表id
     * @return
     */
    int deleteByInsProId(String id);

    /**
     * @Author YangXu
     * @Date 2019/4/30
     * @Descripate  查询通道code
     * @return
     **/
    @Select("select channel_id from institution_channel where ins_pro_id = #{insProId} and enabled = true order by sort")
    List<String> selectChannelCodeByInsProId(@Param("insProId") String insProId);

    List<InstitutionChannel> selectByInsProId(@Param("insProId") String insProId);

    /**
     * 导出机构通道
     *
     * @param searchChannelExportDTO
     * @return
     */
    List<ProductChannelVO> exportProductChannel(SearchChannelExportDTO searchChannelExportDTO);
}
