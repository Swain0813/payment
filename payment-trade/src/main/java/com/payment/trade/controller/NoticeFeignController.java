package com.payment.trade.controller;
import com.payment.common.base.BaseController;
import com.payment.common.dto.NoticeDTO;
import com.payment.common.response.BaseResponse;
import com.payment.trade.feign.NoticeFeign;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * pos公告模块
 */
@RestController
@Api(description = "pos公告管理接口")
@RequestMapping("/notice")
public class NoticeFeignController extends BaseController {

    @Autowired
    private NoticeFeign noticeFeign;

    @ApiOperation(value = "pos机根据语言和公告类别查询公告信息")
    @PostMapping("/posNoticeInfo")
    public BaseResponse posNoticeInfo(@RequestBody @ApiParam NoticeDTO noticeDTO) {
        return noticeFeign.pageNoticeByLanguageAndCategory(noticeDTO);
    }

}
