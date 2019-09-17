package com.payment.finance.controller;
import com.payment.common.dto.SearchAccountCheckDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.finance.service.AccountCheckService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description: 对账接口
 * @author: XuWenQi
 * @create: 2019-03-22 10:10
 **/
@RestController
@Api(description = "对账接口")
@RequestMapping("/finance")
public class AccountCheckController {

    @Autowired
    private AccountCheckService accountCheckService;

    @Value("${file.tmpfile}")
    private String tmpfile;//springboot启动的临时文件存放

    @ApiOperation(value = "通道对账")
    @PostMapping("channelAccountCheck")
    @CrossOrigin
    public BaseResponse channelAccountCheck(@RequestParam("file") @ApiParam MultipartFile file) {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(tmpfile);//指定临时文件路径，这个路径可以随便写
        factory.createMultipartConfig();
        return ResultUtil.success(accountCheckService.ad3ChannelAccountCheck(file));
    }

    @ApiOperation(value = "差错处理")
    @GetMapping("updateCheckAccount")
    public BaseResponse updateCheckAccount(@RequestParam @ApiParam String checkAccountId
            , @RequestParam(required = false) @ApiParam String remark) {
        return ResultUtil.success(accountCheckService.updateCheckAccount(checkAccountId, remark));
    }

    @ApiOperation(value = "差错复核")
    @GetMapping("auditCheckAccount")
    public BaseResponse auditCheckAccount(@RequestParam @ApiParam String checkAccountId
            , @RequestParam @ApiParam Boolean enable, @RequestParam(required = false) @ApiParam String remark) {
        return ResultUtil.success(accountCheckService.auditCheckAccount(checkAccountId, enable, remark));
    }

    @ApiOperation(value = "分页查询对账管理")
    @PostMapping("/pageAccountCheckLog")
    public BaseResponse pageAccountCheckLog(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO) {
        return ResultUtil.success(accountCheckService.pageAccountCheckLog(searchAccountCheckDTO));
    }

    @ApiOperation(value = "分页查询对账管理详情")
    @PostMapping("/pageAccountCheck")
    public BaseResponse pageAccountCheck(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO) {
        return ResultUtil.success(accountCheckService.pageAccountCheck(searchAccountCheckDTO));
    }

    @ApiOperation(value = "分页查询对账管理复核详情")
    @PostMapping("/pageAccountCheckAudit")
    public BaseResponse pageAccountCheckAudit(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO) {
        return ResultUtil.success(accountCheckService.pageAccountCheckAudit(searchAccountCheckDTO));
    }

    @ApiOperation(value = "导出对账管理详情")
    @PostMapping("/exportAccountCheck")
    public BaseResponse exportAccountCheck(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO) {
        return ResultUtil.success(accountCheckService.exportAccountCheck(searchAccountCheckDTO));
    }

    @ApiOperation(value = "导出对账管理复核详情")
    @PostMapping("/exportAccountCheckAudit")
    public BaseResponse exportAccountCheckAudit(@RequestBody @ApiParam SearchAccountCheckDTO searchAccountCheckDTO) {
        return ResultUtil.success(accountCheckService.exportAccountCheckAudit(searchAccountCheckDTO));
    }


}
