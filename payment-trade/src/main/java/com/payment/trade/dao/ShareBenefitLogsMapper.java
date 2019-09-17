package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.ExportAgencyShareBenefitDTO;
import com.payment.common.dto.ExportShareBenefitReportDTO;
import com.payment.common.dto.QueryAgencyShareBenefitDTO;
import com.payment.common.dto.QueryShareBenefitReportDTO;
import com.payment.common.entity.ShareBenefitLogs;
import com.payment.common.vo.QueryAgencyShareBenefitVO;
import com.payment.common.vo.ShareBenefitReportVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShareBenefitLogsMapper extends BaseMapper<ShareBenefitLogs> {

    /**
     * 代理商分润查询
     *
     * @param queryAgencyShareBenefitDTO queryAgencyShareBenefitDTO
     * @return QueryAgencyShareBenefitVO
     */
    List<QueryAgencyShareBenefitVO> pageAgencyShareBenefit(QueryAgencyShareBenefitDTO queryAgencyShareBenefitDTO);

    /**
     * 代理商分润导出
     *
     * @param exportAgencyShareBenefitDTO exportAgencyShareBenefitDTO
     * @return QueryAgencyShareBenefitVO
     */
    List<QueryAgencyShareBenefitVO> exportAgencyShareBenefit(ExportAgencyShareBenefitDTO exportAgencyShareBenefitDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/23
     * @Descripate 根据订单号查询流水是否村咋
     **/
    @Select("select count(1) from share_benefit_logs where order_id = #{orderId}")
    int selectCountByOrderId(@Param("orderId") String orderId);

    /**
     * 运营后台分润报表查询
     *
     * @param queryShareBenefitReportDTO queryShareBenefitReportDTO
     * @return ShareBenefitReportVO
     */
    List<ShareBenefitReportVO> pageShareBenefitReport(QueryShareBenefitReportDTO queryShareBenefitReportDTO);


    /**
     * 运营后台分润报表导出
     *
     * @param exportShareBenefitReportDTO exportShareBenefitReportDTO
     * @return ShareBenefitReportVO
     */
    List<ShareBenefitReportVO> exportShareBenefitReport(ExportShareBenefitReportDTO exportShareBenefitReportDTO);
}
