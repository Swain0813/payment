package com.payment.institution.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.dto.NoticeDTO;
import com.payment.common.vo.NoticeVO;
import com.payment.institution.entity.Notice;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoticeMapper extends BaseMapper<Notice> {
    /**
     * 查询所有的公告信息
     * @param noticeDTO
     * @return
     */
    List<NoticeVO> pageNotice(NoticeDTO noticeDTO);

    /**
     * 根据语言查询公告信息
     * @param noticeDTO
     * @return
     */
    List<NoticeVO> pageNoticeByLanguageAndCategory(NoticeDTO noticeDTO);
}
