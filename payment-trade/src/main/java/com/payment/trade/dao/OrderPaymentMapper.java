package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.OrderPaymentDTO;
import com.payment.common.dto.OrderPaymentExportDTO;
import com.payment.common.entity.OrderPayment;
import com.payment.common.vo.OrderPaymentDetailVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Repository
public interface OrderPaymentMapper extends BaseMapper<OrderPayment> {


    /**
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 分页查询汇款单
     * @return
     **/
    List<OrderPayment> pageFindOrderPayment(OrderPaymentDTO orderPaymentDTO);


    /**
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate getOrderPaymentDetail
     * @return
     **/
    OrderPaymentDetailVO getOrderPaymentDetail(@Param("orderPaymentId") String orderPaymentId,@Param("language") String language);

    /**
     * 根据机构上送的机构订单号查询汇率订单是否存在
     * @param institutionOrderId
     * @return
     */
    @Select("select count(1) from order_payment where institution_order_id = #{institutionOrderId}")
    int selectByInstitutionOrderId(@Param("institutionOrderId") String institutionOrderId);


    /**
     * @Author YangXu
     * @Date 2019/8/9
     * @Descripate 导出汇款单
     * @return
     **/
    List<OrderPayment> exportOrderPayment(OrderPaymentExportDTO orderPaymentExportDTO);
}
