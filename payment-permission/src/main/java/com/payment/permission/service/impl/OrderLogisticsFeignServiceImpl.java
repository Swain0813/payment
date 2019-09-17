package com.payment.permission.service.impl;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.entity.OrderLogistics;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import com.payment.permission.service.OrderLogisticsFeignService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author shenxinran
 * @Date: 2019/6/17 16:30
 * @Description: 批量导入
 */
@Service
public class OrderLogisticsFeignServiceImpl implements OrderLogisticsFeignService {


    /**
     * 机构系统导入订单物流信息
     * @param file
     * @param name
     * @return
     */
    @Override
    public List<OrderLogistics> uploadFiles(MultipartFile file, String name) {
        ArrayList<OrderLogistics> h = new ArrayList<>();
        String fileName = file.getOriginalFilename();
        // 判断格式0
        if (!fileName.matches("^.+\\.(?i)(xls)$") && !fileName.matches("^.+\\.(?i)(xlsx)$")) {
            throw new BusinessException(EResultEnum.FILE_FORMAT_ERROR.getCode());
        }
        ExcelReader reader;
        try {
            reader = ExcelUtil.getReader(file.getInputStream());
        } catch (Exception e) {
            // 当excel内的格式不正确时
            throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
        }

        List<List<Object>> read = reader.read();
        //判断是否超过上传限制
        if (read.size() - 1 > AsianWalletConstant.UPLOAD_LIMIT) {
            throw new BusinessException(EResultEnum.EXCEEDING_UPLOAD_LIMIT.getCode());
        }
        if (read.size() <= 0) {
            throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
        }
        for (int i = 1; i < read.size(); i++) {
            List<Object> objects = read.get(i);
            if (objects.contains(null)) {
                continue;
            }
            //判断传入的excel的格式是否符合约定
            if (StringUtils.isEmpty(objects.get(0))
                    || StringUtils.isEmpty(objects.get(1))
                    || StringUtils.isEmpty(objects.get(2))
                    || StringUtils.isEmpty(objects.get(3))
                    || StringUtils.isEmpty(objects.get(4))) {
                throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
            }
            OrderLogistics ol = new OrderLogistics();
            try {

                ol.setInstitutionCode(objects.get(0).toString().replaceAll("\\s*", ""));
                ol.setInstitutionOrderId(objects.get(1).toString().replaceAll("\\s*", ""));
                ol.setInvoiceNo(objects.get(2).toString().replaceAll("\\s*", ""));
                ol.setProviderName(objects.get(3).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
                ol.setPayerAddress(objects.get(4).toString().replaceAll("/(^\\s*)|(\\s*$)/g", ""));
                if (objects.size() == 6 && !StringUtils.isEmpty(objects.get(5))) {
                    ol.setRemark(objects.get(5).toString().replaceAll("\\s*", ""));
                }
            } catch (Exception e) {
                // 当excel内的格式不正确时
                throw new BusinessException(EResultEnum.EXCEL_FORMAT_INCORRECT.getCode());
            }
            //更新人
            ol.setModifier(name);
            //更新时间
            ol.setUpdateTime(new Date());
            h.add(ol);
        }
        if (h.size() == 0) {
            throw new BusinessException(EResultEnum.IMPORT_REPEAT_ERROR.getCode());
        }
        return h;
    }

}
