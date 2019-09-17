package com.payment.permission.service;

import cn.hutool.poi.excel.ExcelWriter;
import com.payment.common.entity.OrderRefund;
import com.payment.common.entity.Orders;
import com.payment.common.response.BaseResponse;
import com.payment.common.vo.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OrdersFeignService {

    /**
     * Excel 导出功能
     *
     * @param orders 订单实体集合
     * @param clazz  class对象
     * @return ExcelWriter writer
     */
    ExcelWriter getExcelWriter(List<Orders> orders, Class clazz);


    /**
     * Excel 导出退款单功能
     *
     * @param orders 订单实体集合
     * @param clazz  class对象
     * @return ExcelWriter writer
     */
    ExcelWriter getRefundOrderExcelWriter(List<OrderRefund> orders, Class clazz);


    /**
     * Excel 交易一览查询导出功能
     *
     * @param orders
     * @param clazz
     * @return
     */
    ExcelWriter getOrderExcelWriter(List<OrderTradeVO> orders, Class clazz);

    /**
     * 机构交易一览导出功能
     *
     * @param orders
     * @param clazz
     * @return
     */
    ExcelWriter getInstitutionOrderExcelWriter(List<InstitutionOrderTradeVO> orders, Class clazz);

    /**
     * DCC报表导出
     *
     * @param dccReportList
     * @param clazz
     * @return
     */
    ExcelWriter getDccReportExcelWriter(List<DccReportVO> dccReportList, Class<DccReportVO> clazz);


    /**
     * @Author YangXu
     * @Date 2019/8/8
     * @Descripate 导入商户汇款单
     * @return
     **/
    BaseResponse uploadOrderPaymentFiles(MultipartFile file);

    /**
     * @Author YangXu
     * @Date 2019/8/9
     * @Descripate 后台系统用导出汇款单
     * @return
     **/
    ExcelWriter getOrderPaymentExcelWriter(List<OrderPaymentExportVO> list, Class clazz);

    /**
     * 机构后台导出汇款单用
     * @param list
     * @param clazz
     * @return
     */
    ExcelWriter getInsOrderPaymentExcelWriter(List<OrderPaymentInsExportVO> list, Class clazz);

    /**
     * 代理商交易导出
     * @param queryAgencyTradeVOS
     * @param clazz
     * @return
     */
    ExcelWriter exportAgencyTrade(List<QueryAgencyTradeVO> queryAgencyTradeVOS, Class clazz);

    /**
     * 代理商分润导出
     * @param queryAgencyShareBenefitVOS queryAgencyShareBenefitVOS
     * @param clazz
     * @return
     */
    ExcelWriter exportAgencyShareBenefit(List<QueryAgencyShareBenefitVO> queryAgencyShareBenefitVOS, Class clazz);

    /**
     * 运营后台分润报表导出
     * @param shareBenefitReport shareBenefitReport
     * @param clazz
     * @return
     */
    ExcelWriter exportShareBenefitReport(List<ShareBenefitReportVO> shareBenefitReport, Class clazz);
}
