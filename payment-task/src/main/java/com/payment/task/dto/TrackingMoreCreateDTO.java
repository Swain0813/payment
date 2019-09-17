package com.payment.task.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "TrackingMore创建单号请求实体", description = "TrackingMore创建单号请求实体")
public class TrackingMoreCreateDTO {

    @ApiModelProperty(value = "运单号")//必填
    private String tracking_number;

    @ApiModelProperty(value = "快递简码")//必填
    private String carrier_code;

    @ApiModelProperty(value = "目的国二字简码")
    private String destination_code;

    @ApiModelProperty(value = "商品标题")
    private String title;

    @ApiModelProperty(value = "物流渠道")
    private String logistics_channel;

    @ApiModelProperty(value = "客户名称")
    private String customer_name;

    @ApiModelProperty(value = "客户邮箱")
    private String customer_email;

    @ApiModelProperty(value = "客户电话号码")
    private String customer_phone;

    @ApiModelProperty(value = "订单号")
    private String order_id;

    @ApiModelProperty(value = "下单时间(eg:2017/8/27 16:51)")
    private String order_create_time;

    @ApiModelProperty(value = "寄件日期，格式为20181001（年月日），部分运输商（如德国邮政）要求填写发件日期才可以查询物流信息，故查询此类运输商时需要传此参数")
    private String tracking_ship_date;

    @ApiModelProperty(value = "收件地邮编，部分运输商（如Mondial Relay法国快递）要求填写邮编才可以查询物流信息，故查询此类运输商时需要传该参数")
    private String tracking_postal_code;

    @ApiModelProperty(value = "账户号，部分快递要求填写账户号才可以查询物流，故查询此类快递时需要传该参数")
    private String tracking_account_number;

    @ApiModelProperty(value = "目的国参数，部分运输商要求填写目的国家才可以查询物流，故查询此类快递时需要传该参数")
    private String specialNumberDestination;

    @ApiModelProperty(value = "如果将值设置为 1, 系统将不会根据单号自动矫正快递商.")
    private String lang;

    @ApiModelProperty(value = "备注")
    private String auto_correct;

    @ApiModelProperty(value = "目的国二字简码")
    private String comment;


    public TrackingMoreCreateDTO() {

    }

}
