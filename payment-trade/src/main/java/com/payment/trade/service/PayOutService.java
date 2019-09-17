package com.payment.trade.service;

import com.payment.common.dto.OrderPaymentDTO;
import com.payment.common.dto.OrderPaymentExportDTO;
import com.payment.common.dto.PayOutDTO;
import com.payment.common.entity.OrderPayment;
import com.payment.common.response.BaseResponse;
import com.payment.common.vo.OrderPaymentDetailVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 付款服务
 */
public interface PayOutService {


    /**
     * @Author YangXu
     * @Date 2019/7/23
     * @Descripate 批量付款接口
     * @return
     **/
    BaseResponse payment(PayOutDTO payOutDTO);

    /**
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 运维后台审核汇款单
     * @return
     **/
    BaseResponse operationsAudit(String name, String orderPaymentId, boolean enabled, String remark);

    /**
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 分页查询汇款单
     * @return
     **/
    PageInfo<OrderPayment> pageFindOrderPayment(OrderPaymentDTO orderPaymentDTO);


    /**
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 查询汇款单详细信息
     * @return
     **/
    OrderPaymentDetailVO getOrderPaymentDetail(String orderPaymentId,String language);

    /**
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 付款失败调账炒作
     * @return
     **/
     void faliReconciliation(OrderPayment orderPayment,String message);

    /**
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 商户后台汇款接口
     * @return
     **/
    BaseResponse institutionPayment(PayOutDTO payOutDTO);


    /**
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 商户后台审核汇款单接口
     * @return
     **/
    BaseResponse institutionAudit(String name, String orderPaymentId, boolean enabled, String remark);


    /**
     * @Author YangXu
     * @Date 2019/8/9
     * @Descripate 导出汇款单
     * @return
     **/
    List<OrderPayment> exportOrderPayment(OrderPaymentExportDTO orderPaymentExportDTO);


    /**
     * @Author YangXu
     * @Date 2019/8/12
     * @Descripate 人工汇款审核汇款单接口
     * @return
     **/
    BaseResponse artificialPayOutAudit(String name, String orderPaymentId, boolean enabled, String remark);
}
