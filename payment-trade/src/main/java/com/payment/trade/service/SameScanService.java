package com.payment.trade.service;

import com.payment.common.response.BaseResponse;
import com.payment.common.vo.OfflineOrdersVO;
import com.payment.common.vo.PosOrdersVO;
import com.payment.trade.dto.PlaceOrdersDTO;
import com.payment.trade.dto.PosGetOrdersDTO;
import com.payment.trade.dto.TerminalQueryOrdersDTO;
import com.payment.trade.dto.TerminalQueryRelevantDTO;
import com.payment.trade.vo.InstitutionRelevantVO;
import com.payment.trade.vo.OfflineRelevantInfoVO;
import com.payment.trade.vo.TerminalOrderVO;

import java.util.List;

/**
 * @Author XuWenQi
 * @Date 2019/2/12 15:26
 * @Descripate 线下同机构动态扫码业务接口
 */
public interface SameScanService {

    /**
     * 线下同机构CSB动态扫码
     *
     * @param placeOrdersDTO 下单实体
     * @return 通用响应实体
     */
    BaseResponse csbScan(PlaceOrdersDTO placeOrdersDTO);

    /**
     * 线下同机构BSC动态扫码
     *
     * @param placeOrdersDTO 下单实体
     * @return 通用响应实体
     */
    BaseResponse bscScan(PlaceOrdersDTO placeOrdersDTO);

    /**
     * 线下分页查询订单列表
     *
     * @param posGetOrdersDTO 订单输入实体
     * @return 订单输出实体集合
     */
    List<OfflineOrdersVO> terminalQueryOrderList(PosGetOrdersDTO posGetOrdersDTO);


    /**
     * 【内部接口-POS机】分页查询订单列表
     *
     * @param posGetOrdersDTO
     * @return
     */
    List<PosOrdersVO> posQueryOrderList(PosGetOrdersDTO posGetOrdersDTO);


    /**
     * 【内部接口-POS机】查询订单状态接口
     *
     * @param terminalQueryDTO 输入实体
     * @return 订单实体
     */
    TerminalOrderVO terminalQueryOrderStatus(TerminalQueryOrdersDTO terminalQueryDTO);

    /**
     * 【内部接口-POS机】查询订单详情
     *
     * @param terminalQueryDTO 订单输入实体
     * @return 订单输出实体集合
     */
    PosOrdersVO terminalQueryOrderDetail(TerminalQueryOrdersDTO terminalQueryDTO);


    /**
     * 【内部接口-POS机】查询机构关联信息
     * 由于现在aw只看交易币种，所以显示所有AW支持的币种
     * @param terminalQueryDTO
     * @return
     */
    OfflineRelevantInfoVO terminalQueryRelevantInfo(TerminalQueryRelevantDTO terminalQueryDTO);


    /**
     * 【内部接口-收银台】根据机构code查询机构产品,产品通道信息
     *
     * @param institutionCode 机构code
     * @return 订单实体
     */
    List<InstitutionRelevantVO> getRelevantInfo(String institutionCode);
    /**
     * 【内部接口-收银台】根据机构code查询机构产品,产品通道信息
     *
     * @param institutionCode 机构code
     * @return 订单实体
     */
    List<InstitutionRelevantVO> getRelevantInfoSy(String institutionCode);
}
