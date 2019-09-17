package com.payment.permission.feign.institution;

import com.payment.common.dto.DictionaryInfoAllDTO;
import com.payment.common.dto.DictionaryInfoDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.impl.DictionaryInfoFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author shenxinran
 * @Date: 2019/2/1 15:48
 * @Description: 字典类型与数据操作 Feign 接口
 */
@FeignClient(value = "payment-institution", fallback = DictionaryInfoFeignImpl.class)
public interface DictionaryInfoFeign {

    /**
     * 添加字典信息
     *
     * @param dictionaryInfoDTO
     * @return
     */
    @PostMapping("/dictionaryinfo/addDictionaryInfo")
    BaseResponse addDictionaryInfo(@RequestBody @ApiParam DictionaryInfoDTO dictionaryInfoDTO);

    /**
     * 启用禁用字典类型
     *
     * @return
     */
    @PostMapping("/dictionaryinfo/banDictionaryType")
    BaseResponse banDictionaryType(@RequestBody @ApiParam DictionaryInfoDTO dictionaryInfoDTO);


    @PostMapping("/dictionaryinfo/addOtherLanguage")
    BaseResponse addOtherLanguage(@RequestBody @ApiParam DictionaryInfoDTO dictionaryInfoDTO);

    /**
     * 启动禁用字典数据
     *
     * @return
     */
    @PostMapping("/dictionaryinfo/banDictionary")
    BaseResponse banDictionary(@RequestBody @ApiParam DictionaryInfoDTO dictionaryInfoDTO);

    /**
     * 更新字典类型
     *
     * @param dictionaryInfoDTO
     * @return
     */
    @PostMapping("/dictionaryinfo/updateDictionaryType")
    BaseResponse updateDictionaryType(@RequestBody @ApiParam DictionaryInfoDTO dictionaryInfoDTO);

    /**
     * 更新字典数据
     *
     * @param dictionaryInfoDTO
     * @return
     */
    @PostMapping("/dictionaryinfo/updateDictionary")
    BaseResponse updateDictionary(@RequestBody @ApiParam DictionaryInfoDTO dictionaryInfoDTO);

    /**
     * 依据ID查询字典信息
     *
     * @return
     */
    @PostMapping("/dictionaryinfo/getDictionaryInfo")
    BaseResponse getDictionaryInfo(@RequestBody @ApiParam DictionaryInfoDTO dictionaryInfoDTO);

    /**
     * 分页查询字典信息
     *
     * @param dictionaryInfoDTO
     * @return
     */
    @PostMapping("/dictionaryinfo/pageDicTypeInfo")
    BaseResponse pageDicTypeInfo(@RequestBody @ApiParam DictionaryInfoDTO dictionaryInfoDTO);

    /**
     * 查询全部数据字典信息
     * @param dictionaryInfoDTO
     * @return
     */
    @PostMapping("/dictionaryinfo/pageDictionaryInfos")
    BaseResponse pageDictionaryInfos(@RequestBody @ApiParam DictionaryInfoAllDTO dictionaryInfoDTO);


}
