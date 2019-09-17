package com.payment.permission.vo;

import lombok.Data;

import java.util.List;

/**
 * @description: 一级菜单
 * @author: YangXu
 * @create: 2019-01-22 18:00
 **/
@Data
public class FirstMenuVO {
    String id;
    String eName;
    String cName;
    boolean flag = false;//是否选中
    List<com.payment.permission.vo.SecondMenuVO> secondMenuVOS;
}
