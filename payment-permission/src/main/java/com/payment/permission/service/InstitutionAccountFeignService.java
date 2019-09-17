package com.payment.permission.service;

import cn.hutool.poi.excel.ExcelWriter;
import com.payment.common.vo.ExportTradeAccountVO;

public interface InstitutionAccountFeignService {


    /**
     * 机构对账导出功能
     * @param exportTradeAccountVO
     * @param clazz1
     * @param clazz2
     * @return
     */
    ExcelWriter getExcelWriter(ExportTradeAccountVO exportTradeAccountVO,String language,Class clazz1,Class clazz2);
}
