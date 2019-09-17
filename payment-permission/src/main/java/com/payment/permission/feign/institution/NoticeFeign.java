package com.payment.permission.feign.institution;

import com.payment.common.dto.NoticeDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.impl.NoticeFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 公告模块Feign端
 */
@FeignClient(value = "payment-institution", fallback = NoticeFeignImpl.class)
public interface NoticeFeign {
    /**
     * 添加公告信息
     *
     * @param noticeDTO
     * @return
     */
    @PostMapping("/notice/addNotice")
    BaseResponse addNotice(@RequestBody @ApiParam NoticeDTO noticeDTO);

    /**
     * 修改公告信息
     *
     * @param noticeDTO
     * @return
     */
    @PostMapping("/notice/updateNotice")
    BaseResponse updateNotice(@RequestBody @ApiParam NoticeDTO noticeDTO);

    /**
     * 查询所有公告信息
     *
     * @param noticeDTO
     * @return
     */
    @PostMapping("/notice/pageNotice")
    BaseResponse pageNotice(@RequestBody @ApiParam NoticeDTO noticeDTO);

    /**
     * 根据语言和公告类别查询公告信息
     *
     * @param noticeDTO
     * @return
     */
    @PostMapping("/notice/pageNoticeByLanguageAndCategory")
    BaseResponse pageNoticeByLanguageAndCategory(@RequestBody @ApiParam NoticeDTO noticeDTO);
}
