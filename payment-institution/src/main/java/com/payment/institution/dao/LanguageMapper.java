package com.payment.institution.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.dto.LanguageDTO;
import com.payment.institution.entity.Language;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LanguageMapper extends BaseMapper<Language> {


    /**
     * 查找语种
     *
     * @param langCode
     * @param langName
     * @return
     */
    int findLanguage(@Param("langCode") String langCode, @Param("langName") String langName);

    /**
     * 分页查询语种
     *
     * @param languageDTO
     * @return
     */
    List<Language> pageFindLanguage(LanguageDTO languageDTO);


}
