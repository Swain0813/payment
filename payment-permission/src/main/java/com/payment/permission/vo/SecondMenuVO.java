package com.payment.permission.vo;

import lombok.Data;

import java.util.List;

/**
 * @description: 二级菜单
 * @author: YangXu
 * @create: 2019-01-22 18:06
 **/
@Data
public class SecondMenuVO {
    String id;
    String eName;
    String cName;
    boolean flag = false;//是否选中
    List<ThreeMenuVO> threeMenuVOS;
}
