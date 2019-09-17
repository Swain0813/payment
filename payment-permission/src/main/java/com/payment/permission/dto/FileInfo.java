package com.payment.permission.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 文件对象实体
 */
@Data
@ApiModel(value = "文件对象实体", description = "文件对象实体")
public class FileInfo {

    private String oldName;

    private String newName;

    private String path;
}
