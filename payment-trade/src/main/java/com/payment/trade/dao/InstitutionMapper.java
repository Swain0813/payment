package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Institution;
import com.payment.common.vo.DirectConnectionVO;
import com.payment.trade.vo.InstitutionRelevantVO;
import com.payment.trade.vo.InstitutionVO;
import com.payment.trade.vo.OfflineProductVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @Author XuWenQi
 * @Date 2019/2/18 16:20
 * @Descripate 机构Mapper
 */
@Repository
public interface InstitutionMapper extends BaseMapper<Institution> {

    /**
     * 根据商户编号查询商户信息
     *
     * @param institutionCode 商户编号
     * @return 商户输出实体
     */
    Institution selectByInstitutionCode(String institutionCode);


    /**
     * 根据机构编号查询机构关联产品,通道信息
     *
     * @param institutionCode 机构编号
     * @param language
     * @return 关联实体
     */
    InstitutionVO selectRelevantInfo(@Param("institutionCode") String institutionCode, @Param("payMethod") String payMethod, @Param("orderCurrency") String orderCurrency, @Param("tradeDirection") Byte tradeDirection, @Param("language") String language);

    /**
     * 根据机构编号查询线下机构关联产品信息
     *
     * @param institutionCode 机构编号
     * @param tradeDirection  交易方向
     * @param language        语言
     * @return 关联实体
     */
    List<OfflineProductVO> selectProductInfo(@Param("institutionCode") String institutionCode, @Param("tradeDirection") Byte tradeDirection, @Param("language") String language, @Param("dealType") String dealType);

    /**
     * 根据机构code查询机构产品,产品通道信息
     *
     * @param institutionCode 机构code
     * @param language        语言
     * @return 订单实体
     */
    InstitutionVO selectRelevantInfoByInstitutionCode(@Param("institutionCode") String institutionCode, @Param("language") String language);

    /**
     * 根据机构code查询机构产品,产品通道,机构通道信息
     *
     * @param institutionCode 机构code
     * @return 订单实体
     */
    InstitutionRelevantVO getRelevantByInstitutionCode(@Param("institutionCode") String institutionCode, @Param("language") String language);


    /**
     * 根据机构code查询机构产品,产品通道,机构通道信息(启用的)
     *
     * @param institutionCode
     * @param language
     * @return
     */
    InstitutionRelevantVO getRelevantByInstitutionCodeSy(@Param("institutionCode") String institutionCode, @Param("language") String language);

    /**
     * 根据机构code查询机构产品,产品通道信息
     *
     * @param institutionCode 机构code
     * @return 订单实体
     */
    InstitutionRelevantVO getNoRelevantByInstitutionCode(@Param("institutionCode") String institutionCode, @Param("language") String language);

    /**
     * 获取直连所需的信息 通过issuerId与institutionCode去查询
     *
     * @param institutionCode
     * @param issuerId
     * @return
     */
    List<DirectConnectionVO> selectByIssuerIdAndInstitutionId(@Param("institutionCode") String institutionCode, @Param("issuerId") String issuerId);
}
