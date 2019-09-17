package com.payment.permission.service;

import cn.hutool.poi.excel.ExcelWriter;
import com.payment.common.entity.BankIssuerid;
import com.payment.common.vo.ChannelExportVO;

import java.util.List;

/**
 * 通道导出功能
 */
public interface ChannelFeignService {

    /**
     * 通道一览导出功能
     * @param channels
     * @param clazz
     * @return
     */
    ExcelWriter getChannelsExcelWriter(List<ChannelExportVO> channels, Class clazz);

    /**
     * 映射表导出
     *
     * @param bankIssuerids
     * @param clazz
     * @return
     */
    ExcelWriter getBankIssuerWriter(List<BankIssuerid> bankIssuerids, Class clazz);
}
