package com.payment.trade.service;

import com.payment.common.base.BaseService;
import com.payment.common.dto.RefundDTO;
import com.payment.common.dto.SearchOrderDTO;
import com.payment.common.dto.SearchOrderExportDTO;
import com.payment.common.entity.OrderRefund;
import com.payment.common.response.BaseResponse;
import com.github.pagehelper.PageInfo;

import java.util.List;


public interface RefundService extends BaseService<OrderRefund> {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/2/19
     * @Descripate 退款订单接口
     **/
    BaseResponse refundOrder(RefundDTO refundDTO,String ip);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/2/19
     * @Descripate 机构系统退款订单接口
     **/
    BaseResponse refundOrderSys(RefundDTO refundDTO, String ip);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/14
     * @Descripate 退款操作
     **/
    void doRefundOrder(BaseResponse baseResponse,OrderRefund orderRefund);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/14
     * @Descripate 撤销操作
     **/
    void doCancelOrder(BaseResponse baseResponse,OrderRefund orderRefund);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 人工退款接口
     **/
    void artificialRefund(String name, String refundOrderId,Boolean enabled,String remark);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 重复请求退款接口
     **/
    String repeatRefund(String name, String refundOrderId);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 分页查询退款接口
     **/
    PageInfo<OrderRefund> pageRefundOrder(SearchOrderDTO searchOrderDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 导出查询退款接口
     *
     * @param searchOrderDTO*/
    List<OrderRefund> exportRefundOrder(SearchOrderExportDTO searchOrderDTO);

}
