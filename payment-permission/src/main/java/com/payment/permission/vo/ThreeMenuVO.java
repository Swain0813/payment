package com.payment.permission.vo;

import lombok.Data;

/**
 * @description: 三级菜单
 * @author: YangXu
 * @create: 2019-01-22 18:06
 **/
@Data
public class ThreeMenuVO {
    String id;
    String eName;
    String cName;
    boolean flag = false;//是否选中
}
