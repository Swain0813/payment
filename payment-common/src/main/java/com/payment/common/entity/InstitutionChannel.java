package com.payment.common.entity;

import com.payment.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
@Data
@Entity
@Table(name = "institution_channel")
@ApiModel(value = "机构通道中间表", description = "机构通道中间表")
public class InstitutionChannel extends BaseEntity {

    @ApiModelProperty(value = "机构产品中间表id")
    @Column(name = "ins_pro_id")
    private String insProId;

    @ApiModelProperty(value = "通道id")
    @Column(name = "channel_id")
    private String channelId;

    @ApiModelProperty(value = "启用禁用")
    @Column(name = "enabled")
    private Boolean enabled;

    @ApiModelProperty(value = "权重")
    @Column(name = "sort")
    private String sort;
}
