package com.payment.institution.controller;
import com.payment.common.base.BaseController;
import com.payment.common.dto.NoticeDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.institution.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告相关业务的服务
 **/
@RestController
@Api(description ="公告相关接口服务")
@RequestMapping("/notice")
public class NoticeController extends BaseController {

    @Autowired
    private NoticeService noticeService;

    @ApiOperation(value = "添加公告信息")
    @PostMapping("/addNotice")
    public BaseResponse addNotice(@RequestBody @ApiParam NoticeDTO noticeDTO){
        return ResultUtil.success(noticeService.addNotice(this.getSysUserVO().getUsername(),noticeDTO));
    }

    @ApiOperation(value = "修改公告信息")
    @PostMapping("/updateNotice")
    public BaseResponse updateNotice(@RequestBody @ApiParam NoticeDTO noticeDTO){
      return ResultUtil.success(noticeService.updateNotice(this.getSysUserVO().getUsername(),noticeDTO));
    }

    @ApiOperation(value = "查询所有公告信息")
    @PostMapping("/pageNotice")
    public BaseResponse pageNotice(@RequestBody @ApiParam NoticeDTO noticeDTO){
        return ResultUtil.success(noticeService.pageNotice(noticeDTO));
    }

    @ApiOperation(value = "根据语言和公告类别查询公告信息")
    @PostMapping("/pageNoticeByLanguageAndCategory")
    public BaseResponse pageNoticeByLanguageAndCategory(@RequestBody @ApiParam NoticeDTO noticeDTO){
        return ResultUtil.success(noticeService.pageNoticeByLanguageAndCategory(noticeDTO));
    }
}
