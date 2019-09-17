package com.payment.permission.controller;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.RefundDTO;
import com.payment.common.dto.SearchOrderDTO;
import com.payment.common.entity.OrderRefund;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.common.vo.OrderRefundExportVO;
import com.payment.permission.feign.trade.RefundFegin;
import com.payment.permission.service.OperationLogService;
import com.payment.permission.service.OrdersFeignService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @description: 退款
 * @author: YangXu
 * @create: 2019-03-13 15:06
 **/
@RestController
@Api(description = "退款交易管理接口")
@RequestMapping("/refund")
public class RefundFeignController extends BaseController {

    @Autowired
    private RefundFegin refundFegin;

    @Autowired
    private OrdersFeignService ordersFeignService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private RedisService redisService;



    @ApiOperation(value = "人工退款接口")
    @GetMapping("artificialRefund")
    public BaseResponse artificialRefund(@RequestParam @ApiParam String refundOrderId,Boolean enabled,String remark) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(this.getRequest().getParameterMap()),
                "人工退款接口"));
        return refundFegin.artificialRefund(refundOrderId,enabled,remark);
    }

    @ApiOperation(value = "分页查询退款接口")
    @PostMapping("pageRefundOrder")
    public BaseResponse pageRefundOrder(@RequestBody @ApiParam SearchOrderDTO searchOrderDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(searchOrderDTO),
                "分页查询退款接口"));
        return refundFegin.pageRefundOrder(searchOrderDTO);
    }

    @ApiOperation(value = "导出退款接口")
    @PostMapping("exportRefundOrder")
    public BaseResponse exportRefundOrder(@RequestBody @ApiParam SearchOrderDTO searchOrderDTO) {
        BaseResponse baseResponse = refundFegin.exportRefundOrder(searchOrderDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if(data==null || data.size()==0){//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<OrderRefund> OrderRefundList = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            OrderRefundList.add(JSON.parseObject(JSON.toJSONString(datum), OrderRefund.class));
        }
        ExcelWriter writer = null;
        try {
            writer = ordersFeignService.getRefundOrderExcelWriter(OrderRefundList, OrderRefundExportVO.class);
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }

    @ApiOperation(value = "后台系统和机构系统退款接口")
    @PostMapping("refundOrderSys")
    public BaseResponse refundOrderSys(@RequestBody @ApiParam RefundDTO refundDTO,HttpServletRequest request ) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(refundDTO),
                "后台系统和机构系统退款接口"));
        String token = request.getHeader(AsianWalletConstant.tokenHeader);
        if (redisService.get(token) == null) {
            throw new BusinessException(EResultEnum.USER_IS_NOT_LOGIN.getCode());
        }
        refundDTO.setModifier(this.getSysUserVO().getUsername());//更新人
        refundDTO.setToken(token);//设置token
        return refundFegin.refundOrderSys(refundDTO);
    }

}
