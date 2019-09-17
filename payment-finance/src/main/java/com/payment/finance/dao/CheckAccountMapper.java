package com.payment.finance.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.SearchAccountCheckDTO;
import com.payment.finance.entity.CheckAccount;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CheckAccountMapper extends BaseMapper<CheckAccount>{


    /**
     * @Author YangXu
     * @Date 2019/3/29
     * @Descripate 查询所有待对账的记录
     * @return
     **/
    List<CheckAccount> getDataByType(@Param("errorType") int errorType,@Param("startTime") Date startTime,@Param("endTime") Date endTime);

    /**
     * ad3通道对账
     *
     * @param searchAccountCheckDTO 分页查询对账管理详情
     * @return
     */
    List<CheckAccount> pageAccountCheck(SearchAccountCheckDTO searchAccountCheckDTO);

    /**
     * ad3通道对账
     *
     * @param searchAccountCheckDTO 导出对账管理详情
     * @return
     */
    List<CheckAccount> exportAccountCheck(SearchAccountCheckDTO searchAccountCheckDTO);


    /**
     * @Author YangXu
     * @Date 2019/4/10
     * @Descripate 系统补单更新状态
     * @return
     **/
    @Update("update check_account set error_type = 4 ,u_status = 3 ,remark1 = #{remark},update_time=NOW() where u_order_id = #{orderId} and error_type = 3")
    int upateErrorType(@Param("orderId") String orderId,@Param("remark") String remark);

    /**
     * @Author YangXu
     * @Date 2019/4/10
     * @Descripate 查询通道流水号
     * @return
     **/
    @Select("select c_channel_number from check_account where c_order_id = #{orderId}")
    String selectByOrderId(@Param("orderId") String orderId);

    @Select("select count(1) from check_account where error_type = 2 and date_format(#{date}, '%Y-%c-%d' ) = date_format(create_time, '%Y-%c-%d')")
    int getErrorCount(Date date);
}
