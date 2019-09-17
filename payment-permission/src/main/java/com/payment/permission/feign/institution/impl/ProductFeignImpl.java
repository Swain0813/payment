package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.*;
import com.payment.common.entity.Product;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.vo.ExportProductVO;
import com.payment.permission.feign.institution.ProductFeign;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-29 16:29
 **/
@Component
public class ProductFeignImpl implements ProductFeign {

    @Override
    public BaseResponse addProduct(Product product) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse updateProduct(Product product) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse selectProduct(ProductSearchDTO productSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageProduct(ProductSearchDTO productSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse selectProductByPayType(String payType) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse addInstitutionProduct(List<InstitutionProductDTO> institutionProductDtos) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageFindInsProduct(InstitutionProductDTO institutionProductDto) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse exportInsProduct(InstitutionProductDTO institutionProductDto) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageFindInsProductAudit(InstitutionProductDTO institutionProductDto) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getInsProductById(String productId) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getInsProductAuditById(String productId) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getProChannelByInstitutionCode(String institutionCode) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse selectProductByInsCode(String institutionCode) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse updateInfoProduct(InstitutionProductDTO institutionProductDto) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse updateProductEnable(InstitutionProductDTO institutionProductDto) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse auditInfoProduct(AuaditProductDTO auaditProductDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse updateLimitProduct(InstitutionProductDTO institutionProductDto) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse auditLimitProduct(AuaditProductDTO auaditProductDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse allotProductChannel(InstProdDTO instProdDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageFindProductChannel(SearchChannelDTO searchChannelDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse deleteProductChannel(String insChaId) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse banProductChannel( String insChaId, Boolean enabled) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse updateSort(String insChaId, String sort, boolean enabled) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public List<ExportProductVO> exportProduct(ProductSearchExportDTO productSearchDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse batchUpdateSort(List<BatchUpdateSortDTO> batchUpdateSortList) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 导出机构通道
     *
     * @param searchChannelExportDTO
     * @return
     */
    @Override
    public BaseResponse exportProductChannel(SearchChannelExportDTO searchChannelExportDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
