package com.payment.trade.feign;
import com.payment.common.dto.NoticeDTO;
import com.payment.common.response.BaseResponse;
import com.payment.trade.feign.Impl.NoticeFeignImpl;
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
     * 根据语言和公告类别查询公告信息
     *
     * @param noticeDTO
     * @return
     */
    @PostMapping("/notice/pageNoticeByLanguageAndCategory")
    BaseResponse pageNoticeByLanguageAndCategory(@RequestBody @ApiParam NoticeDTO noticeDTO);
}
