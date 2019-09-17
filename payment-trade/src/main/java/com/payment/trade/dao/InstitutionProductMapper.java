package com.payment.trade.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.InstitutionProduct;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitutionProductMapper extends BaseMapper<InstitutionProduct> {
    /**
     * 使用产品CODE 与 机构CODE查询对应的信息
     *
     * @param productCode
     * @param institutionCode
     * @return
     */
    InstitutionProduct selectByProductCodeAndInstitutionCode(@Param("productCode") Integer productCode, @Param("institutionCode") String institutionCode);

    /**
     * 根据机构id和产品id查询
     *
     * @param institutionId 机构id
     * @param productId     产品 id
     * @return 机构产品
     */
    InstitutionProduct selectByInstitutionIdAndProductId(@Param("institutionId") String institutionId, @Param("productId") String productId);
}
