package com.payment.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-04-15 18:09
 **/
@Data
@ApiModel(value = "pos机交易打印查询输出实体", description = "pos机交易打印查询输出实体")
public class PosSearchLogVO {

    @ApiModelProperty(value = "付款方式")
    @Column(name = "pay_method")
    private String payMethod;

    @ApiModelProperty(value = "pos机交易打印查询输出实体")
    private List<PosSearchVO> posSearchVO;
}
