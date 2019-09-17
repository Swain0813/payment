package com.payment.permission.controller;

import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.redis.RedisService;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 退出登录
 */
@Api(description = "退出登录")
@RestController
@RequestMapping("/auth")
public class LogoutController extends BaseController{

    @Autowired
    private RedisService redisService;

    @ApiOperation(value = "退出登录")
    @GetMapping("/logout")
    public BaseResponse logout(HttpServletRequest request ) {
        String token = request.getHeader(AsianWalletConstant.tokenHeader);
        if(StringUtils.isNotBlank(token)){
            redisService.set(token, JSON.toJSONString(this.getSysUserVO()),1);
        }
        return ResultUtil.success();
    }
}
