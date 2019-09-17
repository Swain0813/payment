package com.payment.trade.controller;
import com.payment.common.base.BaseController;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.trade.dto.UndoDTO;
import com.payment.trade.service.CancelOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * 撤销服务(线下)
 *
 */
@RestController
@Api(description = "撤销服务")
@RequestMapping("/trade")
public class CancelOrderController extends BaseController {

    @Autowired
    private CancelOrderService cancelOrderService;

    @ApiOperation(value = "撤销当前指定的订单")
    @PostMapping("/undo")
    public BaseResponse undo(@RequestBody @ApiParam @Valid UndoDTO undoDTO){
        BaseResponse baseResponse = cancelOrderService.undo(undoDTO);
        String code = baseResponse.getCode();//业务返回码
        if (StringUtils.isEmpty(code)) {
            baseResponse.setCode(EResultEnum.SUCCESS.getCode());
            baseResponse.setMsg("SUCCESS");
            return baseResponse;
        }
        return ResultUtil.error(code, this.getErrorMsgMap(code));
    }
}
