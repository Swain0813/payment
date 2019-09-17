package com.payment.permission.controller;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.permission.service.FileUploadService;
import com.payment.permission.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * 文件上传
 */
@RestController
@Api(description ="文件上传接口")
@RequestMapping("/upload")
public class FileUploadController extends BaseController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private OperationLogService operationLogService;

    @Value("${file.tmpfile}")
    private String tmpfile;//springboot启动的临时文件存放

    @ApiOperation(value = "图片上传")
    @PostMapping("/image")
    public BaseResponse uploadImage(@RequestParam(value = "file",required = false) @ApiParam MultipartFile file){
        //添加操作日志
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, null,
                "图片上传"));
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(tmpfile);//指定临时文件路径，这个路径可以随便写
        factory.createMultipartConfig();
        return ResultUtil.success(fileUploadService.uploadImage(file));
    }
}
