package com.payment.permission.controller;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.OrderPaymentDTO;
import com.payment.common.dto.OrderPaymentExportDTO;
import com.payment.common.dto.PayOutDTO;
import com.payment.common.entity.OrderPayment;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.common.vo.OrderPaymentExportVO;
import com.payment.common.vo.OrderPaymentInsExportEnVO;
import com.payment.common.vo.OrderPaymentInsExportVO;
import com.payment.common.vo.SysUserVO;
import com.payment.permission.feign.trade.OrdersPaymentFeign;
import com.payment.permission.service.OperationLogService;
import com.payment.permission.service.OrdersFeignService;
import com.payment.permission.service.SysUserVoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @description: 汇款管理
 * @author: YangXu
 * @create: 2019-08-07 16:17
 **/
@RestController
@Api(description = "汇款单管理接口")
@RequestMapping("/payOut")
public class OrderPaymentFeignController extends BaseController {

    @Autowired
    private OrdersFeignService ordersFeignService;

    @Autowired
    private OrdersPaymentFeign ordersPaymentFeign;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private SysUserVoService sysUserVoService;

    @Value("${file.tmpfile}")
    private String tmpfile;//springboot启动的临时文件存放

    @ApiOperation(value = "分页查询汇款单")
    @PostMapping("/pageFindOrderPayment")
    public BaseResponse pageFindOrderPayment(@RequestBody @ApiParam OrderPaymentDTO orderPaymentDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, null,
                "分页查询汇款单"));
        return ordersPaymentFeign.pageFindOrderPayment(orderPaymentDTO);
    }

    @ApiOperation(value = "查询汇款单详细信息")
    @GetMapping("/getOrderPaymentDetail")
    public BaseResponse getOrderPaymentDetail(@RequestParam @ApiParam String orderPaymentId) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, null,
                "查询汇款单详细信息"));
        return ordersPaymentFeign.getOrderPaymentDetail(orderPaymentId, this.getLanguage());
    }

    @ApiOperation(value = "运维审核汇款单接口")
    @GetMapping("/operationsAudit")
    public BaseResponse operationsAudit(@RequestParam @ApiParam String orderPaymentId, @RequestParam @ApiParam boolean enabled,
                                        @RequestParam @ApiParam String remark, @RequestParam @ApiParam String tradePwd) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, null,
                "运维审核汇款单接口"));
        SysUserVO sysUserVO = this.getSysUserVO();
        if (!sysUserVoService.checkPassword(sysUserVoService.decryptPassword(tradePwd), sysUserVO.getTradePassword())) {
            throw new BusinessException(EResultEnum.TRADE_PASSWORD_ERROR.getCode());
        }
        return ordersPaymentFeign.operationsAudit(sysUserVO.getName(), orderPaymentId, enabled, remark);
    }

    @ApiOperation(value = "商户后台审核汇款单接口")
    @GetMapping("/institutionAudit")
    public BaseResponse institutionAudit(@RequestParam @ApiParam String orderPaymentId, @RequestParam @ApiParam boolean enabled,
                                         @RequestParam @ApiParam String remark, @RequestParam @ApiParam String tradePwd) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, null,
                "商户后台审核汇款单接口"));
        SysUserVO sysUserVO = this.getSysUserVO();
        if (!sysUserVoService.checkPassword(sysUserVoService.decryptPassword(tradePwd), sysUserVO.getTradePassword())) {
            throw new BusinessException(EResultEnum.TRADE_PASSWORD_ERROR.getCode());
        }
        return ordersPaymentFeign.institutionAudit(this.getSysUserVO().getUsername(), orderPaymentId, enabled, remark);
    }


    @ApiOperation(value = "人工汇款审核汇款单接口")
    @GetMapping("/artificialPayOutAudit")
    public BaseResponse artificialPayOutAudit(@RequestParam @ApiParam String orderPaymentId, @RequestParam @ApiParam boolean enabled,
                                              @RequestParam @ApiParam String remark, @RequestParam @ApiParam String tradePwd){
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, null,
                "商户后台审核汇款单接口"));
        SysUserVO sysUserVO = this.getSysUserVO();
        if (!sysUserVoService.checkPassword(sysUserVoService.decryptPassword(tradePwd), sysUserVO.getTradePassword())) {
            throw new BusinessException(EResultEnum.TRADE_PASSWORD_ERROR.getCode());
        }
        return  ordersPaymentFeign.artificialPayOutAudit(this.getSysUserVO().getUsername(),orderPaymentId,enabled,remark);
    }

    @ApiOperation(value = "商户后台单笔/批量汇款接口")
    @PostMapping("/institutionPayment")
    public BaseResponse institutionPayment(@Valid @RequestBody @ApiParam PayOutDTO payOutDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, null,
                "商户后台单笔/批量汇款接口"));
        SysUserVO sysUserVO = this.getSysUserVO();
        if (!sysUserVoService.checkPassword(sysUserVoService.decryptPassword(payOutDTO.getTradePwd()), sysUserVO.getTradePassword())) {
            throw new BusinessException(EResultEnum.TRADE_PASSWORD_ERROR.getCode());
        }
        String reqIp = getReqIp();
        payOutDTO.setReqIp(reqIp);
        return ordersPaymentFeign.institutionPayment(payOutDTO);
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/8
     * @Descripate
     **/
    @ApiOperation(value = "导入商户汇款单")
    @PostMapping("uploadOrderPaymentFiles")
    public BaseResponse uploadOrderPaymentFiles(@RequestParam("file") @ApiParam MultipartFile file) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, null,
                "导入商户汇款单"));
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(tmpfile);//指定临时文件路径，这个路径可以随便写
        factory.createMultipartConfig();
        return ordersFeignService.uploadOrderPaymentFiles(file);
    }

    /**
     * 后台管理系统汇款查询一览导出
     * @param orderPaymentDTO
     * @return
     */
    @ApiOperation(value = "后台管理系统导出汇款单")
    @PostMapping("/exportOrderPayment")
    public BaseResponse exportOrderPayment(@RequestBody @ApiParam OrderPaymentExportDTO orderPaymentDTO) {
        BaseResponse baseResponse = ordersPaymentFeign.exportOrderPayment(orderPaymentDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<OrderPaymentExportVO> list = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            list.add(JSON.parseObject(JSON.toJSONString(datum), OrderPaymentExportVO.class));
        }
        ExcelWriter writer = null;
        try {
            writer = ordersFeignService.getOrderPaymentExcelWriter(list, OrderPaymentExportVO.class);
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (IOException e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        }finally {
            writer.close();
        }
        return ResultUtil.success();
    }

    /**
     * 机构后台系统导出汇款单
     * @param orderPaymentDTO
     * @return
     */
    @ApiOperation(value = "机构后台系统导出汇款单")
    @PostMapping("/exportInsOrderPayment")
    public BaseResponse exportInsOrderPayment(@RequestBody @ApiParam OrderPaymentExportDTO orderPaymentDTO) {
        BaseResponse baseResponse = ordersPaymentFeign.exportOrderPayment(orderPaymentDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<OrderPaymentInsExportVO> list = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            list.add(JSON.parseObject(JSON.toJSONString(datum), OrderPaymentInsExportVO.class));
        }
        ExcelWriter writer = null;
        try {
            if (AsianWalletConstant.EN_US.equals(this.getLanguage())) {//英文的场合
                writer = ordersFeignService.getInsOrderPaymentExcelWriter(list, OrderPaymentInsExportEnVO.class);
            }else {
                writer = ordersFeignService.getInsOrderPaymentExcelWriter(list, OrderPaymentInsExportVO.class);
            }
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (IOException e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        }finally {
            writer.close();
        }
        return ResultUtil.success();
    }

}
