package com.payment.trade.service.impl;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.LogisticsBachDTO;
import com.payment.common.dto.OrderLogisticsBachDTO;
import com.payment.common.dto.OrderLogisticsDTO;
import com.payment.common.dto.OrderLogisticsQueryDTO;
import com.payment.common.entity.Courier;
import com.payment.common.entity.OrderLogistics;
import com.payment.common.enums.Status;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import com.payment.trade.cache.CourierRedisService;
import com.payment.trade.dao.OrderLogisticsMapper;
import com.payment.trade.dto.OrderLogisticsBatchQueryDTO;
import com.payment.trade.service.CommonService;
import com.payment.trade.service.OrderLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.Date;
import java.util.List;

/**
 * 订单物流信息相关服务
 */
@Service
@Transactional
@Slf4j
public class OrderLogisticsServiceImpl implements OrderLogisticsService {

    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CourierRedisService courierRedisService;

    /**
     * 查询订单物流信息
     *
     * @param orderLogisticsQueryDTO
     * @return
     */
    @Override
    public List<OrderLogistics> getOrderLogisticsInfo(OrderLogisticsQueryDTO orderLogisticsQueryDTO) {
        return orderLogisticsMapper.getOrderLogisticsInfo(orderLogisticsQueryDTO);
    }

    /**
     * 修改订单物流信息
     *
     * @param name
     * @param orderLogisticsDTO
     * @return
     */
    @Override
    public int updateOrderLogistics(String name, OrderLogisticsDTO orderLogisticsDTO) {
        //返回结果
        int result = 0;
        //根据订单物流id查询订单物流信息是不是存在
        OrderLogistics orderLogistics = orderLogisticsMapper.getOrderLogisticsInfoById(orderLogisticsDTO);
        if (orderLogistics == null) {
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());//数据不存在
        }
        //更新订单物流信息
        orderLogistics.setInvoiceNo(orderLogisticsDTO.getInvoiceNo());//发货单号
        orderLogistics.setProviderName(orderLogisticsDTO.getProviderName());//服务商名称
        orderLogistics.setCourierCode(this.getCourierCodeInfo(orderLogisticsDTO.getProviderName()));//运输商简码
        orderLogistics.setPayerAddress(orderLogisticsDTO.getPayerAddress());//收货人地址
        orderLogistics.setId(orderLogistics.getId());//物流id
        orderLogistics.setDeliveryStatus(TradeConstant.SHIPPED);//已发货
        orderLogistics.setModifier(name);//更新人
        orderLogistics.setUpdateTime(new Date());//更新时间
        orderLogistics.setRemark(orderLogisticsDTO.getRemark());//备注
        result = orderLogisticsMapper.updateByPrimaryKeySelective(orderLogistics);//更新订单物流信息
        if (result > 0) {//更新成功的成功
            //物流信息更新后发送发货通知邮件
            this.commonService.sendDeliveryEmail(orderLogistics.getPayerEmail(), orderLogistics.getLanguage(), Status._2, orderLogistics);
        }
        return result;
    }

    /**
     * 批量修改订单物流信息----对外提供的api
     *
     * @param orderLogisticsDTO
     * @return
     */
    @Override
    public int updateOrderLogisticsBatch(OrderLogisticsBachDTO orderLogisticsDTO) {
        int reslut = 0;
        //校验签名
        if (!commonService.checkOrderLogistics(orderLogisticsDTO)) {
            throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
        }
        //输入参数的check
        List<LogisticsBachDTO> lists = orderLogisticsDTO.getLogisticsBachDTOs();
        if (lists == null || lists.size() == 0) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        for (LogisticsBachDTO list : lists) {
            //根据订单物流id判断是不是存在
            OrderLogistics orderLogistics = orderLogisticsMapper.selectByPrimaryKey(list.getId());
            if (orderLogistics == null) {
                throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());//数据不存在
            }
            orderLogistics.setInvoiceNo(list.getInvoiceNo());//发货单号
            orderLogistics.setProviderName(list.getProviderName());//服务商名称
            orderLogistics.setCourierCode(this.getCourierCodeInfo(list.getProviderName()));//运输商简码
            orderLogistics.setPayerAddress(list.getPayerAddress());//收货人地址
            orderLogistics.setDeliveryStatus(TradeConstant.SHIPPED);//已发货
            orderLogistics.setModifier(orderLogistics.getInstitutionName());//更新人
            orderLogistics.setUpdateTime(new Date());//更新时间
            orderLogistics.setRemark(list.getRemark());//备注
            reslut += orderLogisticsMapper.updateByPrimaryKeySelective(orderLogistics);
            if (reslut > 0) {//物流更新成功的场合，物流信息更新后发送发货通知邮件
                this.commonService.sendDeliveryEmail(orderLogistics.getPayerEmail(), orderLogistics.getLanguage(), Status._2, orderLogistics);
            }
        }
        return reslut;
    }

    /**
     * 机构物流信息批量导入
     *
     * @param fileList
     */
    @Override
    public int uploadFiles(List<OrderLogistics> fileList) {
        OrderLogistics logistics = null;
        int flag = 0;
        for (OrderLogistics orderLogistics : fileList) {
            logistics = orderLogisticsMapper.selectByinstitutionOrderIdAndInstitutionCode(orderLogistics);
            if (StringUtils.isEmpty(logistics)) {
                continue;
            }
            logistics.setInvoiceNo(orderLogistics.getInvoiceNo());//发货单号
            logistics.setProviderName(orderLogistics.getProviderName());//服务商名称
            logistics.setUpdateTime(orderLogistics.getUpdateTime());
            logistics.setModifier(orderLogistics.getModifier());
            logistics.setCourierCode(this.getCourierCodeInfo(orderLogistics.getProviderName()));//运输商简码
            logistics.setPayerAddress(orderLogistics.getPayerAddress());//收货人地址
            if (!StringUtils.isEmpty(logistics.getRemark())) {
                logistics.setRemark(orderLogistics.getRemark());//备注
            }
            logistics.setDeliveryStatus(TradeConstant.SHIPPED);//已发货
            flag += orderLogisticsMapper.updateByPrimaryKeySelective(logistics);
            if (flag > 0) {//物流更新成功的场合，物流信息更新后发送发货通知邮件
                this.commonService.sendDeliveryEmail(logistics.getPayerEmail(), logistics.getLanguage(), Status._2, logistics);
            }
        }
        return flag;
    }

    /**
     * 机构批量查询订单物流信息
     *
     * @param orderLogisticsBatchQueryDTO
     * @return
     */
    @Override
    public List<OrderLogistics> getOrderLogisticsInfos(OrderLogisticsBatchQueryDTO orderLogisticsBatchQueryDTO) {
        //commonService.generateSignatureUseInst(orderLogisticsBatchQueryDTO);//获取签名明文
        //验签
        if (!commonService.checkSignMsgWithRSAMD5(orderLogisticsBatchQueryDTO)) {
            throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
        }
        return orderLogisticsMapper.getOrderLogisticsInfos(orderLogisticsBatchQueryDTO);
    }


    /**
     * 根据服务商名称获取运输商简码
     * @param providerName
     * @return
     */
    private String getCourierCodeInfo(String providerName){
        String courierCode =null;
        if(!StringUtils.isEmpty(providerName)){
            List<Courier> courierLists = courierRedisService.getCourierLists();
            for(Courier courierList:courierLists){
                if(courierList.getCourierCnName().toUpperCase().contains(providerName.toUpperCase())
                        ||courierList.getCourierEnName().toUpperCase().contains(providerName.toUpperCase())){
                    courierCode= courierList.getCourierCode();
                }
            }
        }
        return courierCode;
    }

}
