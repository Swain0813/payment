package com.payment.institution.service;
import com.payment.common.dto.NoticeDTO;
import com.payment.common.vo.NoticeVO;
import com.payment.institution.entity.Notice;
import com.github.pagehelper.PageInfo;

/**
 * 公告模块相关业务
 */
public interface NoticeService{
    /**
     * 添加公告信息
     * @param userName
     * @param noticeDTO
     * @return
     */
    int addNotice(String userName,NoticeDTO noticeDTO);

    /**
     * 修改公告信息
     * @param userName
     * @param noticeDTO
     * @return
     */
    int updateNotice(String userName, NoticeDTO noticeDTO);

    /**
     * 查询所有公告信息
     * @param noticeDTO
     * @return
     */
    PageInfo<NoticeVO> pageNotice(NoticeDTO noticeDTO);

    /**
     * 根据语言和公告类别查询启用的公告信息
     * @param noticeDTO
     * @return
     */
    PageInfo<NoticeVO> pageNoticeByLanguageAndCategory(NoticeDTO noticeDTO);
}
