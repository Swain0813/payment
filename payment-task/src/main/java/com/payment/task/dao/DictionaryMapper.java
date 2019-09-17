package com.payment.task.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Dictionary;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DictionaryMapper extends BaseMapper<Dictionary> {
    /**
     * 返回对应类型的详细信息code
     *
     * @return
     */
    List<String> getCodeWithDicTypeCode(@Param("dictypeCode") String dictypeCode, @Param("language") String language);
}
