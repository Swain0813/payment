package com.payment.clearing.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.ShareBenefitLogs;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShareBenefitLogsMapper extends BaseMapper<ShareBenefitLogs> {


    /**
     * @Author YangXu
     * @Date 2019/8/23
     * @Descripate 查询所有已结算未分润的订单
     * @return
     **/
    List<ShareBenefitLogs> selectbyStStatusAndIsShare();

    /**
     * @Author YangXu
     * @Date 2019/8/27
     * @Descripate
     * @return
     **/
    @Update("update share_benefit_logs set is_share = #{isShare},update_time= NOW() where id = #{id} and is_share = 1")
    int updateByIsShare(@Param("id") String id, @Param("isShare") Byte isShare);

    @Select("select count(1) from share_benefit_logs where id = #{id} and is_share = 1")
    int selectCountbyIdAndIsShare(@Param("id") String id, @Param("isShare") Byte isShare);
}
