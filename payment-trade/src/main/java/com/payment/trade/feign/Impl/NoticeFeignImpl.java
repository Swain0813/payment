package com.payment.trade.feign.Impl;

import com.payment.common.dto.NoticeDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.trade.feign.NoticeFeign;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 公告模块Feign端的实现类
 */
@Component
public class NoticeFeignImpl implements NoticeFeign {
    /**
     * 根据语言和公告类别查询公告信息
     *
     * @param noticeDTO
     * @return
     */
    @Override
    public BaseResponse pageNoticeByLanguageAndCategory(@RequestBody @ApiParam NoticeDTO noticeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
