package com.payment.permission.service;

import cn.hutool.poi.excel.ExcelWriter;
import com.payment.common.entity.SettleOrder;
import com.payment.permission.dto.SettleOrderExport;
import com.payment.permission.dto.SettleOrderInsExport;

import java.util.ArrayList;

/**
 * 机构结算
 */
public interface SettleOrderFeignService {

    /**
     * 机构结算表的导出
     * @param settleOrder
     * @param settleOrderExportClass
     * @return
     */
    ExcelWriter getExcelWriter(ArrayList<SettleOrder> settleOrder, Class<SettleOrderExport> settleOrderExportClass);

    /**
     * 机构后台用机构结算表导出
     * @param settleOrder
     * @param clazz
     * @return
     */
    ExcelWriter getInsExcelWriter(ArrayList<SettleOrder> settleOrder, Class clazz);

}
