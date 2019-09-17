package com.payment.task.scheduled;

import com.alibaba.fastjson.JSON;
import com.payment.common.constant.TradeConstant;
import com.payment.common.entity.OrderLogistics;
import com.payment.common.utils.ArrayUtil;
import com.payment.task.dao.OrderLogisticsMapper;
import com.payment.task.dto.TrackingMoreCreateDTO;
import com.payment.task.utils.Tracker;
import com.payment.task.vo.TrackingMoreItemsVO;
import com.payment.task.vo.TrackingMoreVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单物流信息签收状态更新定时任务
 *
 * @author: XuWenQi
 * @create: 2019-07-02 15:33
 **/
@Component
@Slf4j
@Api(value = "订单物流信息签收状态更新定时任务")
public class LogisticsTask {

    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;

    /**
     * 定时更新订单物流信息状态
     */
    @Transactional
    @Scheduled(cron = "0 0 3 ? * *")// 每天早上3点执行一次
    //@Scheduled(cron = "0/10 * * * * ? ")//每10秒执行一次 测试用
    public void updateLogistics() {
        log.info("******************【订单物流信息签收状态更新】定时任务开始执行****************");
        //查询未签收的所有订单集合
        List<OrderLogistics> noReceivedList = orderLogisticsMapper.getNoReceivedList();
        if (ArrayUtil.isEmpty(noReceivedList)) {
            log.info("******************【未签收状态】订单物流信息为空****************");
            return;
        }
        //按指定数量分组
        List<List<OrderLogistics>> paramsList = groupListByQuantity(noReceivedList, 40);
        for (List<OrderLogistics> orderLogistics : paramsList) {
            List<TrackingMoreCreateDTO> createDTOList = new ArrayList<>();//账号创建物流订单请求List
            List<String> numbers = new ArrayList<>();//查询物流单号List
            for (OrderLogistics o : orderLogistics) {
                if (StringUtils.isEmpty(o.getRemark())) {
                    //标记为空时,添加到创建物流快递单号集合
                    TrackingMoreCreateDTO trackingMoreCreateDTO = new TrackingMoreCreateDTO();
                    trackingMoreCreateDTO.setTracking_number(o.getInvoiceNo());//运单号
                    trackingMoreCreateDTO.setCarrier_code(o.getCourierCode());//快递简码
                    createDTOList.add(trackingMoreCreateDTO);//每个物流订单实体
                }
                numbers.add(o.getInvoiceNo());//运单号
            }
            //调用TrackingMore批量创建物流订单接口
            if (!ArrayUtil.isEmpty(createDTOList)) {
                String createResult = new Tracker().orderOnlineByJson(JSON.toJSONString(createDTOList), null, "batch");
                log.info("*************调用【TrackingMore批量创建订单接口】响应结果记录************* createResult: {}", createResult);
                //更新标记
                orderLogisticsMapper.updateRemark(createDTOList);
            }
            //拼接TrackingMore批量查询物流订单接口
            StringBuilder sb = new StringBuilder();
            //已签收的
            sb.append("?status=delivered");
            sb.append("&numbers=");
            //拼接查询接口URL
            for (int i = 0; i < numbers.size(); i++) {
                if (i < numbers.size() - 1) {
                    sb.append(numbers.get(i)).append(",");
                } else {
                    sb.append(numbers.get(i));
                }
            }
            String urlStr = sb.toString();
            log.info("**************调用【TrackingMore批量查询接口】请求URL记录************** urlStr:{}", urlStr);
            //调用TrackingMore物流信息批量查询接口
            String result = new Tracker().orderOnlineByJson(null, urlStr, "get");
            //解析返回结果
            TrackingMoreVO trackingMoreVO = JSON.parseObject(result, TrackingMoreVO.class);
            log.info("***************JSON解析后的【TrackingMore批量查询接口】响应结果记录***********trackingMoreVO: {}", JSON.toJSONString(trackingMoreVO));
            if (trackingMoreVO == null) {
                log.info("*************调用【TrackingMore批量查询接口】响应结果为空****************");
                return;
            }
            if (!TradeConstant.HTTP_SUCCESS.equals(trackingMoreVO.getMeta().getCode())) {
                log.info("*************调用【TrackingMore批量查询接口】CODE不为200**************** meta:{}", JSON.toJSONString(trackingMoreVO.getMeta()));
                return;
            }
            //获取成功物流单号的数据
            List<String> invoiceNoList = new ArrayList<>();//已签收物流订单号List
            for (TrackingMoreItemsVO itemsVO : trackingMoreVO.getData().getItems()) {
                //添加签收成功的发货单号
                invoiceNoList.add(itemsVO.getTracking_number());//运单号
            }
            //批量更新签收状态
            int updateNumbers = orderLogisticsMapper.updateReceivedByInvoiceNo(invoiceNoList);
            log.info("******************【订单物流信息签收状态更新】****************更新签收状态的数据条数: {}", updateNumbers);
        }
        log.info("******************【订单物流信息签收状态更新】定时任务结束执行****************");
    }

    /**
     * 将集合按指定数量分组
     *
     * @param list     数据集合
     * @param quantity 分组数量
     * @return 分组结果
     */
    private List<List<OrderLogistics>> groupListByQuantity(List<OrderLogistics> list, int quantity) {
        List<List<OrderLogistics>> wrapList = new ArrayList<>();
        int count = 0;
        while (count < list.size()) {
            wrapList.add(new ArrayList<>(list.subList(count, (count + quantity) > list.size() ? list.size() : count + quantity)));
            count += quantity;
        }
        return wrapList;
    }
}
