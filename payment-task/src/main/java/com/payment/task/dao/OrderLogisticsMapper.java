package com.payment.task.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.OrderLogistics;
import com.payment.task.dto.TrackingMoreCreateDTO;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderLogisticsMapper extends BaseMapper<OrderLogistics> {

    /**
     * 查询当日更新的已签收的订单物流信息
     *
     * @return
     */
    List<OrderLogistics> getOrderLogistics();

    /**
     * 查询未签收的订单
     *
     * @return
     */
    List<OrderLogistics> getNoReceivedList();

    /**
     * 根据发货单号更新签收状态
     *
     * @return
     */
    int updateReceivedByInvoiceNo(List<String> ids);

    /**
     * 调用第三方接口需要创建 更新标记
     * 1表示已创建 第二次再去调时就不要重新创建了
     *
     * @return
     */
    int updateRemark(List<TrackingMoreCreateDTO> logistics);

}
