package com.payment.institution.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.vo.ProChannelVO;
import com.payment.institution.entity.ProductChannel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductChannelMapper extends BaseMapper<ProductChannel> {

    /**
     * 根据通道id删除产品通道中间表的记录
     * @param channelId
     */
    @Delete("DELETE FROM product_channel WHERE channel_id = #{channelId}")
    void deleteByChannelId(@Param("channelId") String channelId);

    /**
     * 根据机构编码查询机构所有产品
     * @param institutionCode
     * @param language
     * @return
     */
    List<ProChannelVO> getProChannelByInstitutionCode(@Param("institutionCode") String institutionCode,@Param("language") String language);
}
