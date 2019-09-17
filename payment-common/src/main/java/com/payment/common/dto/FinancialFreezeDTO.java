package com.payment.common.dto;
import com.payment.common.entity.Account;
import com.payment.common.entity.Reconciliation;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;

/**
 * @author shenxinran
 * @Date: 2019/6/19 10:27
 * @Description: 清结算资金冻结解冻输入实体
 */
@Data
@ApiModel(value = "清结算资金冻结解冻输入实体", description = "清结算资金冻结解冻输入实体")
public class FinancialFreezeDTO {

    @ApiModelProperty(value = "版本号")
    private String version = "v1.0";

    @ApiModelProperty(value = "字符集")
    private String inputCharset;

    @ApiModelProperty(value = "语言，1中文 2英文")
    private String language;

    @ApiModelProperty(value = "商户号")
    private String merchantId;

    @ApiModelProperty(value = "虚拟账户编号")
    private String mvaccountId;

    @ApiModelProperty(value = "备注信息")
    private String desc;

    @ApiModelProperty(value = "商户订单号")
    private String merOrderNo;

    @ApiModelProperty(value = "交易币种")
    private String txncurrency;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty(value = "交易金额")
    private String txnamount;

    @ApiModelProperty(value = "状态 1加冻结，2解冻结")
    private String state;

    @ApiModelProperty(value = "签名文字符串")
    private String signMsg;

    @ApiModelProperty(value = "调账记录表的id")//留作队列处理更新用
    private String reconciliationId;

    public FinancialFreezeDTO(Reconciliation reconciliation, Account account) {
        this.reconciliationId = reconciliation.getId();
        this.inputCharset = "1";
        this.language = "1";
        this.merchantId = reconciliation.getInstitutionCode();
        this.mvaccountId = account.getId();//账户 id
        this.desc = reconciliation.getReconciliationType() == 3 ? "加冻结" : "解冻结";
        this.merOrderNo = account.getId();//账户 id
        this.txncurrency = reconciliation.getOrderCurrency();
        this.txnamount = String.valueOf(reconciliation.getTradeAmount().setScale(2, BigDecimal.ROUND_HALF_UP));//订单金额,2位
        this.state = reconciliation.getReconciliationType() == 3 ? "1" : "2"; //系统调账--3冻结 4 解冻 ||清结算--1:冻结2:解冻
    }


    public FinancialFreezeDTO() {
    }
}
