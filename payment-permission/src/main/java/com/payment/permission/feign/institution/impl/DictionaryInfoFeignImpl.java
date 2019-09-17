package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.DictionaryInfoAllDTO;
import com.payment.common.dto.DictionaryInfoDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.institution.DictionaryInfoFeign;
import org.springframework.stereotype.Component;

/**
 * @author shenxinran
 * @Date: 2019/2/1 15:49
 * @Description: 字典类型与数据操作熔断类
 */
@Component
public class DictionaryInfoFeignImpl implements DictionaryInfoFeign {
    /**
     * 添加字典信息
     *
     * @param dictionaryInfoDTO
     * @return
     */
    @Override
    public BaseResponse addDictionaryInfo(DictionaryInfoDTO dictionaryInfoDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 启用禁用字典类型
     *
     * @return
     */
    @Override
    public BaseResponse banDictionaryType(DictionaryInfoDTO dictionaryInfoDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse addOtherLanguage(DictionaryInfoDTO dictionaryInfoDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 启动禁用字典数据
     *
     * @return
     */
    @Override
    public BaseResponse banDictionary(DictionaryInfoDTO dictionaryInfoDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 更新字典类型
     *
     * @param dictionaryInfoDTO
     * @return
     */
    @Override
    public BaseResponse updateDictionaryType(DictionaryInfoDTO dictionaryInfoDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 更新字典数据
     *
     * @param dictionaryInfoDTO
     * @return
     */
    @Override
    public BaseResponse updateDictionary(DictionaryInfoDTO dictionaryInfoDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 依据ID查询字典信息
     */
    @Override
    public BaseResponse getDictionaryInfo(DictionaryInfoDTO dictionaryInfoDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 分页查询字典信息
     *
     * @param dictionaryInfoDTO
     * @return
     */
    @Override
    public BaseResponse pageDicTypeInfo(DictionaryInfoDTO dictionaryInfoDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 查询全部数据字典信息
     * @param dictionaryInfoDTO
     * @return
     */
    @Override
    public BaseResponse pageDictionaryInfos(DictionaryInfoAllDTO dictionaryInfoDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
