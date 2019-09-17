package com.payment.trade.controller;
import com.payment.common.base.BaseController;
import com.payment.common.dto.OrderPaymentDTO;
import com.payment.common.dto.OrderPaymentExportDTO;
import com.payment.common.dto.PayOutDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.trade.channels.help2pay.Help2PayService;
import com.payment.trade.service.CommonService;
import com.payment.trade.service.PayOutService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @description: 付款接口
 * @author: YangXu
 * @create: 2019-07-22 16:03
 **/
@Slf4j
@RestController
@Api(description = "付款接口")
@RequestMapping("/payOut")
public class PayOutController extends BaseController {

    @Autowired
    private Help2PayService help2PayService;

    @Autowired
    private PayOutService payOutService;

    @Autowired
    private CommonService commonService;

    /**
     * 对外的api接口
     *
     * @param payOutDTO
     * @return
     */
    @ApiOperation(value = "付款API接口")
    @PostMapping("/payment")
    public BaseResponse payment(@Valid @RequestBody @ApiParam PayOutDTO payOutDTO) {
        String reqIp = getReqIp();
        payOutDTO.setReqIp(reqIp);
        return payOutService.payment(payOutDTO);
    }

    /**
     * 机构后台用汇款接口
     *
     * @param payOutDTO
     * @return
     */
    @ApiOperation(value = "商户后台付款接口")
    @PostMapping("/institutionPayment")
    public BaseResponse institutionPayment(@Valid @RequestBody @ApiParam PayOutDTO payOutDTO) {
        return payOutService.institutionPayment(payOutDTO);
    }

    @ApiOperation(value = "运维审核汇款单接口")
    @GetMapping("/operationsAudit")
    public BaseResponse operationsAudit(@RequestParam @ApiParam String name, @RequestParam @ApiParam String orderPaymentId, @RequestParam @ApiParam boolean enabled, @RequestParam @ApiParam String remark) {
        return payOutService.operationsAudit(name, orderPaymentId, enabled, remark);
    }

    @ApiOperation(value = "商户后台审核汇款单接口")
    @GetMapping("/institutionAudit")
    public BaseResponse institutionAudit(@RequestParam @ApiParam String name, @RequestParam @ApiParam String orderPaymentId, @RequestParam @ApiParam boolean enabled, @RequestParam @ApiParam String remark) {
        return payOutService.institutionAudit(name, orderPaymentId, enabled, remark);
    }

    @ApiOperation(value = "人工汇款审核汇款单接口")
    @GetMapping("/artificialPayOutAudit")
    public BaseResponse artificialPayOutAudit(@RequestParam @ApiParam String name, @RequestParam @ApiParam String orderPaymentId, @RequestParam @ApiParam boolean enabled, @RequestParam @ApiParam String remark) {
        return payOutService.artificialPayOutAudit(name, orderPaymentId, enabled, remark);
    }


    @ApiOperation(value = "help2Pay验证接口")
    @PostMapping("/verification")
    public String verification(HttpServletRequest request) {
        String transId = request.getParameter("transId");
        String key = request.getParameter("key");
        log.info("------------------help2Pay验证接口----------------transId : {}, key : {}", transId, key);
        return help2PayService.verification(transId, key);
    }

    @ApiOperation(value = "付款接口创建签名")
    @PostMapping("/createSign")
    public String createSign(@Valid @RequestBody @ApiParam PayOutDTO payOutDTO) {
        return commonService.generateListSignaturePay(payOutDTO);
    }

    @ApiOperation(value = "分页查询汇款单")
    @PostMapping("/pageFindOrderPayment")
    public BaseResponse pageFindOrderPayment(@RequestBody @ApiParam OrderPaymentDTO orderPaymentDTO) {
        return ResultUtil.success(payOutService.pageFindOrderPayment(orderPaymentDTO));
    }

    @ApiOperation(value = "查询汇款单详细信息")
    @GetMapping("/getOrderPaymentDetail")
    public BaseResponse getOrderPaymentDetail(@RequestParam @ApiParam String orderPaymentId, @RequestParam @ApiParam String language) {
        return ResultUtil.success(payOutService.getOrderPaymentDetail(orderPaymentId, language));
    }

    @ApiOperation(value = "导出汇款单")
    @PostMapping("/exportOrderPayment")
    public BaseResponse exportOrderPayment(@RequestBody @ApiParam OrderPaymentExportDTO orderPaymentExportDTO) {
        return ResultUtil.success(payOutService.exportOrderPayment(orderPaymentExportDTO));
    }

}
