package com.payment.permission.service;

import com.payment.common.entity.OrderLogistics;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OrderLogisticsFeignService {
    /**
     * 机构系统导入订单物流信息
     * @param file
     * @param name
     * @return
     */
    List<OrderLogistics> uploadFiles(MultipartFile file, String name);
}
