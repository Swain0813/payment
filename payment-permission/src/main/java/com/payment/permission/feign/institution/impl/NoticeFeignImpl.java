package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.NoticeDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.institution.NoticeFeign;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 公告模块Feign端的实现类
 */
@Component
public class NoticeFeignImpl implements NoticeFeign {

    /**
     * 添加公告信息
     *
     * @param noticeDTO
     */
    @Override
    public BaseResponse addNotice(@RequestBody @ApiParam NoticeDTO noticeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 修改公告信息
     *
     * @param noticeDTO
     * @return
     */
    @Override
    public BaseResponse updateNotice(@RequestBody @ApiParam NoticeDTO noticeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 查询所有公告信息
     *
     * @param noticeDTO
     * @return
     */
    @Override
    public BaseResponse pageNotice(@RequestBody @ApiParam NoticeDTO noticeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

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
