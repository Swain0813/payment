package com.payment.common.entity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 运输商简码表
 */
@Data
@Entity
@Table(name = "courier")
@ApiModel(value = "运输商简码表", description = "运输商简码表")
public class Courier {

    @ApiModelProperty(value = "id")
    @Id
    @Column(name = "id")
    private Integer id;

    @ApiModelProperty(value = "运输商简码")
    @Column(name = "courier_code")
    private String courierCode;

    @ApiModelProperty(value = "运输商英文名称")
    @Column(name = "courier_en_name")
    private String courierEnName;

    @ApiModelProperty(value = "运输商中文名称")
    @Column(name = "courier_cn_name")
    private String courierCnName;

}
