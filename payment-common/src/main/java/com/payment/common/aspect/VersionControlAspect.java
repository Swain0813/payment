package com.payment.common.aspect;

import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 版本控制切面类
 */
@Aspect
@Component
@Slf4j
public class VersionControlAspect {

    @Autowired
    private RedisService redisService;

//    /**
//     * 权限服务Controller切面
//     */
//    @Pointcut("execution(public * com.payment.permission.controller.*.export*(..))")
//    private void versionControlExport() {
//    }

    /**
     * 权限服务Controller切面
     */
    @Pointcut("execution(public * com.payment.permission.controller.*.*(..))")
    private void versionControlP() {
    }

    /**
     * 交易服务Controller切面
     */
    @Pointcut("execution(public * com.payment.trade.controller.*.*(..))")
    private void versionControlT() {
    }

    /**
     * 账务服务Controller切面
     */
    @Pointcut("execution(public * com.payment.finance.controller.*.*(..))")
    private void versionControlF() {
    }

    /**
     * Common异常类切面
     */
    @Pointcut("execution(public * com.payment.common.exception.GlobalExceptionHandler.goalException(..))")
    private void versionControlE() {
    }


    /**
     * 方法执行后执行
     */
    @AfterReturning(value = "versionControlP()")
    public void doAroundAdviceP() {
        setVersionHeader();
    }

    /**
     * 方法执行后执行
     */
    @AfterReturning(value = "versionControlT()")
    public void doAroundAdviceT() {
        setVersionHeader();
    }

    /**
     * 方法执行后执行
     */
    @AfterReturning(value = "versionControlF()")
    public void doAroundAdviceF() {
        setVersionHeader();
    }

    /**
     * 方法执行后执行
     */
    @AfterReturning(value = "versionControlE()")
    public void doAroundAdviceE() {
        setVersionHeader();
    }


    /**
     * 设置前端版本响应头
     */
    private void setVersionHeader() {
        String version = redisService.get(AsianWalletConstant.VERSION_CONTROL);
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        log.info("============VersionControl切面============ 统一设置响应头 Version-Control:{}", version);
        response.setHeader("Version-Control", version);
        //使浏览器不过滤自定义响应头
        response.setHeader("Access-Control-Expose-Headers", "Version-Control");
    }


}
