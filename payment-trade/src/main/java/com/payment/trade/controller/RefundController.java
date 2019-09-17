package com.payment.trade.controller;
import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.RefundDTO;
import com.payment.common.dto.SearchOrderDTO;
import com.payment.common.dto.SearchOrderExportDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.common.vo.SysUserVO;
import com.payment.trade.feign.SysUserFeign;
import com.payment.trade.service.RefundService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;


/**
 * @description: 退款
 * @author: YangXu
 * @create: 2019-02-15 16:32
 **/
@RestController
@Api(description = "退款")
@RequestMapping("/refund")
public class RefundController extends BaseController {

    @Autowired
    private RefundService refundService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private RedisService redisService;

    @ApiOperation(value = "退款接口")
    @PostMapping("refundOrder")
    @CrossOrigin
    public BaseResponse refundOrder(@RequestBody @ApiParam @Valid RefundDTO refundDTO) {
        //线下判断交易密码
        if (TradeConstant.TRADE_UPLINE.equals(refundDTO.getTradeDirection())) {
            if(StringUtils.isEmpty(refundDTO.getToken())){
                throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
            }
            SysUserVO sysUserVO = JSON.parseObject(redisService.get(refundDTO.getToken()), SysUserVO.class);
            if(sysUserVO==null){//获取不到用户信息
                throw new BusinessException(EResultEnum.USER_IS_NOT_LOGIN.getCode());
            }
            if (sysUserFeign.checkPassword(refundDTO.getTradePassword(), sysUserVO.getTradePassword()).getData().equals("false")) {
                throw new BusinessException(EResultEnum.TRADE_PASSWORD_ERROR.getCode());
            }
        }
        BaseResponse baseResponse = refundService.refundOrder(refundDTO, this.getReqIp());
        if (StringUtils.isEmpty(baseResponse.getMsg())) {
            baseResponse.setCode(EResultEnum.SUCCESS.getCode());
            baseResponse.setMsg("SUCCESS");
            return baseResponse;
        } else {
            return ResultUtil.error(baseResponse.getMsg(), this.getErrorMsgMap(baseResponse.getMsg()));
        }
    }

    @ApiOperation(value = "后台系统和机构系统退款接口")
    @PostMapping("refundOrderSys")
    @CrossOrigin
    public BaseResponse refundOrderInstitution(@RequestBody @ApiParam @Valid RefundDTO refundDTO) {
        //判断交易密码
        SysUserVO sysUserVO = JSON.parseObject(redisService.get(refundDTO.getToken()), SysUserVO.class);
        if (sysUserFeign.checkPassword(refundDTO.getTradePassword(), sysUserVO.getTradePassword()).getData().equals("false")) {
            throw new BusinessException(EResultEnum.TRADE_PASSWORD_ERROR.getCode());
        }
        BaseResponse baseResponse = refundService.refundOrderSys(refundDTO, this.getReqIp());
        if (StringUtils.isEmpty(baseResponse.getMsg())) {
            baseResponse.setCode(EResultEnum.SUCCESS.getCode());
            baseResponse.setMsg("SUCCESS");
            return baseResponse;
        } else {
            return ResultUtil.error(baseResponse.getMsg(), this.getErrorMsgMap(baseResponse.getMsg()));
        }
    }


    @ApiOperation(value = "人工退款接口")
    @GetMapping("artificialRefund")
    public BaseResponse artificialRefund(@RequestParam @ApiParam String refundOrderId, Boolean enabled,String remark) {
        refundService.artificialRefund(this.getSysUserVO().getUsername(), refundOrderId, enabled,remark);
        return ResultUtil.success(null);
    }

    @ApiOperation(value = "重复请求退款接口")
    @GetMapping("repeatRefund")
    public BaseResponse repeatRefund(@RequestParam @ApiParam String refundOrderId) {
        return ResultUtil.success(refundService.repeatRefund(this.getSysUserVO().getUsername(), refundOrderId));
    }


    @ApiOperation(value = "分页查询退款接口")
    @PostMapping("pageRefundOrder")
    public BaseResponse pageRefundOrder(@RequestBody @ApiParam SearchOrderDTO searchOrderDTO) {
        return ResultUtil.success(refundService.pageRefundOrder(searchOrderDTO));
    }

    @ApiOperation(value = "导出退款接口")
    @PostMapping("exportRefundOrder")
    public BaseResponse exportRefundOrder(@RequestBody @ApiParam SearchOrderExportDTO searchOrderDTO) {
        return ResultUtil.success(refundService.exportRefundOrder(searchOrderDTO));
    }

}
