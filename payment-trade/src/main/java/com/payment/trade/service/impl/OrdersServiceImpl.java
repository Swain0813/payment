package com.payment.trade.service.impl;

import com.alibaba.fastjson.JSON;
import com.payment.common.config.AuditorProvider;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.*;
import com.payment.common.entity.Orders;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.ArrayUtil;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.vo.*;
import com.payment.trade.dao.DictionaryMapper;
import com.payment.trade.dao.InstitutionMapper;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dao.ShareBenefitLogsMapper;
import com.payment.trade.dto.CalcRateDTO;
import com.payment.trade.service.CommonService;
import com.payment.trade.service.OrdersService;
import com.payment.trade.vo.ProductVO;
import com.payment.trade.vo.*;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * @Author XuWenQi
 * @Date 2019/2/18 15:14
 * @Descripate 订单接口实现类
 */
@Slf4j
@Service
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private AuditorProvider auditorProvider;

    @Autowired
    private DictionaryMapper dictionaryMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private InstitutionMapper institutionMapper;

    @Autowired
    private ShareBenefitLogsMapper shareBenefitLogsMapper;

    /**
     * 多条件查询订单信息
     *
     * @param ordersDTO 订单输入实体
     * @return 订单实体集合
     */
    @Override
    public PageInfo<OrdersVO> getByMultipleConditions(OrdersDTO ordersDTO) {
        //查询订单只能查询自己机构的订单信息，所以必须输入机构code
        if (StringUtils.isEmpty(ordersDTO.getInstitutionCode())) {
            throw new BusinessException(EResultEnum.INSTITUTIONCODE_IS_NULL.getCode());
        }
        //获取当前请求的语言
        ordersDTO.setLanguage(auditorProvider.getLanguage());//设置语言
        List<OrdersVO> ordersVOS = ordersMapper.pageSelectMultipleConditions(ordersDTO);
        return new PageInfo(ordersVOS);
    }


    /**
     * 订单导出
     *
     * @param ordersDTO 订单输入实体
     * @return 订单输出实体集合
     */
    @Override
    public List<Orders> exportInformation(OrdersDTO ordersDTO) {
        ordersDTO.setLanguage(auditorProvider.getLanguage());//设置语言
        List<Orders> orders = ordersMapper.selectExport(ordersDTO);
        if (orders == null || orders.size() == 0) {
            throw new BusinessException(EResultEnum.ORDER_NOT_EXIST.getCode());
        }
        for (Orders order : orders) {
            order.setProductName(order.getPayMethod().concat("-").concat(order.getOrderCurrency()));
            if (order.getRateType() == null) {
                order.setRateType("");
            } else if (order.getRateType().equals(TradeConstant.FEE_TYPE_RATE)) {
                order.setRateType("单笔费率");
            } else {
                order.setRateType("单笔定额");
            }
            if (order.getChannelFeeType() == null) {
                order.setChannelFeeType("");
            } else if (order.getChannelFeeType().equals(TradeConstant.FEE_TYPE_RATE)) {
                order.setChannelFeeType("单笔费率");
            } else {
                order.setChannelFeeType("单笔定额");
            }
            if (order.getChannelGatewayFeeType() == null) {
                order.setChannelGatewayFeeType("");
            } else if (order.getChannelGatewayFeeType().equals(TradeConstant.FEE_TYPE_RATE)) {
                order.setChannelGatewayFeeType("单笔费率");
            } else {
                order.setChannelGatewayFeeType("单笔定额");
            }
        }
        return orders;
    }

    /**
     * 交易明细查询
     *
     * @param id 订单输入实体
     * @return 订单明细输出实体集合
     */
    @Override
    public TradeDetailVO getTradeDetail(String id) {
        String language = auditorProvider.getLanguage();
        TradeDetailVO tradeDetail = ordersMapper.getTradeDetail(id, language);
        //产品名称
        if (tradeDetail != null && tradeDetail.getDName() == null) {
            tradeDetail.setPayName("");
        } else {
            tradeDetail.setPayName(tradeDetail.getDName().concat("-").concat(tradeDetail.getTradeCurrency()));
        }
        return tradeDetail;
    }

    /**
     * 换汇金额计算
     *
     * @param calcRateDTO 订单输入实体
     * @return 换汇计算输出实体
     */
    @Override
    public BaseResponse calcExchangeRate(CalcRateDTO calcRateDTO) {
        log.info("---------收银台换汇开始---------CalcRateDTO:{}", JSON.toJSON(calcRateDTO));
        InstitutionVO institutionVO = institutionMapper.selectRelevantInfo(calcRateDTO.getInstitutionCode(), calcRateDTO.getPayType(), calcRateDTO.getOrderCurrency(), TradeConstant.TRADE_ONLINE, null);
        if (institutionVO.getProductList().size() == 0) {
            throw new BusinessException(EResultEnum.INSTITUTION_PRODUCT_STATUS_ABNORMAL.getCode());
        }
        ProductVO product = institutionVO.getProductList().get(0);
        BigDecimal ipFloatRate = product.getIpFloatRate();
        CashierCalcRateVO calcRateVO = new CashierCalcRateVO();
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(EResultEnum.SUCCESS.getCode());
        String defaultValue = dictionaryMapper.selectByCurrency(calcRateDTO.getTradeCurrency());
        if (StringUtils.isEmpty(defaultValue)) {
            log.info("-----------换汇错误 结束换汇 交易币种不存在-----------CalcRateDTO:{}", JSON.toJSON(calcRateDTO));
            throw new BusinessException(EResultEnum.PRODUCT_CURRENCY_NO_SUPPORT.getCode());
        }
        if (calcRateDTO.getOrderCurrency().equals(calcRateDTO.getTradeCurrency())) {
            log.info("---------同币种---------");
            calcRateVO.setExchangeTime(new Date());
            int bitPos = defaultValue.indexOf(".");
            int numOfBits = 0;
            if (bitPos != -1) {
                numOfBits = defaultValue.length() - bitPos - 1;
            }
            calcRateVO.setTradeAmount(String.valueOf(calcRateDTO.getAmount().setScale(numOfBits, BigDecimal.ROUND_HALF_UP)));
            calcRateVO.setExchangeStatus(TradeConstant.SWAP_SUCCESS);
            calcRateVO.setOriginalRate(BigDecimal.ONE);//原始汇率
            baseResponse.setData(calcRateVO);
            return baseResponse;
        }
        CalcRateVO crv = commonService.calcExchangeRate(calcRateDTO.getOrderCurrency(), calcRateDTO.getTradeCurrency(), ipFloatRate, calcRateDTO.getAmount());
        BigDecimal tradeAmount = crv.getTradeAmount();
        if (tradeAmount == null) {
            log.info("-----------换汇错误 结束换汇-----------CalcRateDTO:{}", JSON.toJSON(calcRateDTO));
            throw new BusinessException(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
        }
        calcRateVO.setExchangeRate(crv.getExchangeRate());
        calcRateVO.setExchangeStatus(crv.getExchangeStatus());
        calcRateVO.setExchangeTime(crv.getExchangeTime());
        calcRateVO.setOriginalRate(crv.getOriginalRate());//原始汇率
        int bitPos = defaultValue.indexOf(".");
        int numOfBits = 0;
        if (bitPos != -1) {
            numOfBits = defaultValue.length() - bitPos - 1;
        }
        calcRateVO.setTradeAmount(String.valueOf(crv.getTradeAmount().setScale(numOfBits, BigDecimal.ROUND_HALF_UP)));
        baseResponse.setData(calcRateVO);
        baseResponse.setMsg("SUCCESS");
        log.info("---------收银台换汇结束---------");
        return baseResponse;
    }


    /**
     * 计算机构不同产品品的每日成功订单的总订单金额,与产品总金额
     *
     * @param calcOrdersAmountDTO 订单输入实体
     * @return
     */
    @Override
    public StatisticsVO calcOrdersAmount(CalcOrdersAmountDTO calcOrdersAmountDTO) {
        //默认为一个月
        if (StringUtils.isEmpty(calcOrdersAmountDTO.getStartDate()) && StringUtils.isEmpty(calcOrdersAmountDTO.getEndDate())) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            //一个月前的时间
            cal.add(Calendar.MONTH, -1);
            //开始日期
            calcOrdersAmountDTO.setStartDate(DateToolUtils.getReqDate(cal.getTime()));
            //结束日期
            calcOrdersAmountDTO.setEndDate(DateToolUtils.getReqDate(new Date()));
        }
        //语言
        calcOrdersAmountDTO.setLanguage(auditorProvider.getLanguage());
        //查询不同产品的每日金额
        List<CalcInsOrdersAmountVO> dailies = ordersMapper.calcInsOrdersDailyAmount(calcOrdersAmountDTO);
        if (ArrayUtil.isEmpty(dailies)) {
            //暂无交易记录
            throw new BusinessException(EResultEnum.SUCCESS_TRADE_IS_NOT_EXIST.getCode());
        }
        //查询不同支付方式的总金额
        List<CalcTotalAmountVO> payTypeTotals = ordersMapper.calcInsOrdersTotalAmount(calcOrdersAmountDTO);
        //查询不同币种的总金额
        List<CalcCurrencyAmountVO> currencyTotals = ordersMapper.selectCurrencyTotalAmount(calcOrdersAmountDTO);
        StatisticsVO statisticsVO = new StatisticsVO();
        statisticsVO.setDailyAmountList(dailies);
        statisticsVO.setTotalCurrencyAmountList(currencyTotals);
        statisticsVO.setTotalPayTypeAmountList(payTypeTotals);
        return statisticsVO;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/8
     * @Descripate pos机查询订单
     **/
    @Override
    public List<PosSearchVO> posGetOrders(PosSearchDTO posSearchDTO) {
        return ordersMapper.posGetOrders(posSearchDTO);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/8
     * @Descripate pos机查询订单详情
     **/
    @Override
    public List<PosSearchVO> posGetOrdersDetail(PosSearchDTO posSearchDTO) {
        return ordersMapper.posGetOrdersDetail(posSearchDTO);
    }


    /**
     * 分页多条件查询相关订单全部信息(订单表,退款表，调账表)
     *
     * @param ordersAllDTO
     * @return
     */
    @Override
    public PageInfo<OrderTradeVO> getAllOrdersInfo(OrdersAllDTO ordersAllDTO) {
        //获取当前请求的语言
        ordersAllDTO.setLanguage(auditorProvider.getLanguage());//设置语言
        List<OrderTradeVO> ordersVOS = ordersMapper.pageGetAllOrdersInfo(ordersAllDTO);
        return new PageInfo(ordersVOS);
    }

    /**
     * 交易一览的导出功能
     *
     * @param ordersAllDTO 订单输入实体
     * @return
     */
    @Override
    public List<OrderTradeVO> exportAllOrders(OrdersExportAllDTO ordersAllDTO) {
        //获取当前请求的语言
        ordersAllDTO.setLanguage(auditorProvider.getLanguage());//设置语言
        return ordersMapper.exportAllOrdersInfo(ordersAllDTO);
    }

    /**
     * 机构交易一览导出
     *
     * @param ordersAllDTO
     * @return
     */
    @Override
    public List<InstitutionOrderTradeVO> exportInstitutionOrders(OrdersExportAllDTO ordersAllDTO) {
        //获取当前请求的语言
        ordersAllDTO.setLanguage(auditorProvider.getLanguage());//设置语言
        return ordersMapper.exportInstitutionOrders(ordersAllDTO);
    }

    /**
     * DCC报表查询
     *
     * @param dccReportDTO dcc报表查询实体
     * @return DccReportVO
     */
    @Override
    public PageInfo<DccReportVO> getDccReport(DccReportDTO dccReportDTO) {
        List<DccReportVO> dccReportList = ordersMapper.pageDccReport(dccReportDTO);
        if (dccReportList != null && dccReportList.size() != 0) {
            for (DccReportVO dccReportVO : dccReportList) {
                if (!StringUtils.isEmpty(dccReportVO.getOldExchangeRate())) {
                    //浮动金额=交易金额-订单金额*原始汇率
                    dccReportVO.setFloatAmount(dccReportVO.getTradeAmount().subtract(dccReportVO.getAmount().multiply(new BigDecimal(dccReportVO.getOldExchangeRate()))).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
            }
        }
        return new PageInfo<>(dccReportList);
    }

    /**
     * DCC报表导出
     *
     * @param dccReportExportDTO dcc报表查询实体
     * @return DccReportVO
     */
    @Override
    public List<DccReportVO> exportDccReport(DccReportExportDTO dccReportExportDTO) {
        List<DccReportVO> dccReportList = ordersMapper.exportDccReport(dccReportExportDTO);
        if (dccReportList != null && dccReportList.size() != 0) {
            for (DccReportVO dccReportVO : dccReportList) {
                if (!StringUtils.isEmpty(dccReportVO.getOldExchangeRate())) {
                    //浮动金额=交易金额-订单金额*原始汇率
                    dccReportVO.setFloatAmount(dccReportVO.getTradeAmount().subtract(dccReportVO.getAmount().multiply(new BigDecimal(dccReportVO.getOldExchangeRate()).setScale(2, BigDecimal.ROUND_HALF_UP))));
                }
            }
        }
        return dccReportList;
    }


    /**
     * 代理商交易查询
     *
     * @param queryAgencyTradeDTO 代理商交易查询DTO
     * @return
     */
    @Override
    public PageInfo<QueryAgencyTradeVO> getAgencyTrade(QueryAgencyTradeDTO queryAgencyTradeDTO) {
        return new PageInfo<>(ordersMapper.pageAgencyTrade(queryAgencyTradeDTO));
    }


    /**
     * 代理商交易导出
     *
     * @param exportAgencyTradeDTO 代理商交易查询DTO
     * @return QueryAgencyTradeVO
     */
    @Override
    public List<QueryAgencyTradeVO> exportAgencyTrade(ExportAgencyTradeDTO exportAgencyTradeDTO) {
        return ordersMapper.exportAgencyTrade(exportAgencyTradeDTO);
    }

    /**
     * 代理商分润查询
     *
     * @param queryAgencyShareBenefitDTO queryAgencyShareBenefitDTO
     * @return QueryAgencyTradeVO
     */
    @Override
    public PageInfo<QueryAgencyShareBenefitVO> getAgencyShareBenefit(QueryAgencyShareBenefitDTO queryAgencyShareBenefitDTO) {
        return new PageInfo<>(shareBenefitLogsMapper.pageAgencyShareBenefit(queryAgencyShareBenefitDTO));
    }


    /**
     * 代理商分润导出
     *
     * @param exportAgencyShareBenefitDTO exportAgencyShareBenefitDTO
     * @return QueryAgencyShareBenefitVO
     */
    @Override
    public List<QueryAgencyShareBenefitVO> exportAgencyShareBenefit(ExportAgencyShareBenefitDTO exportAgencyShareBenefitDTO) {
        return shareBenefitLogsMapper.exportAgencyShareBenefit(exportAgencyShareBenefitDTO);
    }

    /**
     * 运营后台分润报表查询
     *
     * @param queryShareBenefitReportDTO queryShareBenefitReportDTO
     * @return ShareBenefitReportVO
     */
    @Override
    public PageInfo<ShareBenefitReportVO> getShareBenefitReport(QueryShareBenefitReportDTO queryShareBenefitReportDTO) {
        return new PageInfo<>(shareBenefitLogsMapper.pageShareBenefitReport(queryShareBenefitReportDTO));
    }


    /**
     * 运营后台分润报表导出
     *
     * @param exportShareBenefitReportDTO exportShareBenefitReportDTO
     * @return ShareBenefitReportVO
     */
    @Override
    public List<ShareBenefitReportVO> exportShareBenefitReport(ExportShareBenefitReportDTO exportShareBenefitReportDTO) {
        return shareBenefitLogsMapper.exportShareBenefitReport(exportShareBenefitReportDTO);
    }
}
