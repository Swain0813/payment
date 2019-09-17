package com.payment.institution.service;

import com.payment.common.base.BaseService;
import com.payment.common.dto.*;
import com.payment.common.entity.Product;
import com.payment.common.response.BaseResponse;
import com.payment.common.vo.*;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ProductService extends BaseService<Product> {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 插入产品
     **/
    int addProduct(String creator, Product product);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 更新产品
     **/
    int updateProduct(String creator, Product product);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 分页查询产品
     **/
    PageInfo<ProductVO> pageProduct(ProductSearchDTO productSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 查询所有产品
     **/
    List<ProductVO> selectProduct(ProductSearchDTO productSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据支付方式查询所有产品
     **/
    List<Product> selectProductByPayType(String payType, String language);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据机构号Code查询所有产品
     **/
    List<InstitutionProductVO> selectProductByInsCode(String language, String institutionCode);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 机构添加产品信息
     **/
    String addInstitutionProduct(String creator, List<InstitutionProductDTO> institutionProductDtos);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 分页查询机构产品信息
     **/
    PageInfo<InstitutionProductVO> pageFindInsProduct(InstitutionProductDTO institutionProductDto);

    /**
     * @param institutionProductDto
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 导出机构产品信息
     */
    List<InstitutionProductVO> exportInsProduct(InstitutionProductExportDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 分页查询机构产品审核信息
     **/
    PageInfo<InstitutionProductVO> pageFindInsProductAudit(InstitutionProductDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据产品Id查询产品详情
     **/
    InstitutionProductVO getInsProductById(String insProductId, String language);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据产品Id查询产品审核详情
     **/
    InstitutionProductVO getInsProductAuditById(String insProductId, String language);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据机构编码查询机构所有产品
     **/
    List<ProChannelVO> getProChannelByInstitutionCode(String institutionCode, String language);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 修改产品信息
     **/
    int updateInfoProduct(String modifier, InstitutionProductDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 批量审核产品信息
     **/
    BaseResponse auditInfoProduct(String modifier, AuaditProductDTO auaditProductDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 修改产品限额
     **/
    int updateLimitProduct(String modifier, InstitutionProductDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 启用禁用机构产品
     **/
    int updateProductEnable(String modifier, InstitutionProductDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 批量审核产品限额
     **/
    int auditLimitProduct(String modifier, AuaditProductDTO auaditProductDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 产品分配通道
     **/
    int allotProductChannel(String modifier, InstProdDTO instProdDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 分页查询产品通道管理信息
     **/
    PageInfo<ProductChannelVO> pageFindProductChannel(SearchChannelDTO searchChannelDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 删除产品通道管理信息
     **/
    int deleteProductChannel(String insChaId);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 启用禁用产品通道
     **/
    int banProductChannel(String insChaId, Boolean enabled);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 修改机构通道优先级
     **/
    int updateSort(String modifier, String insChaId, String sort, boolean enabled);

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/5/24
     * @Descripate 导出产品信息
     **/
    List<ExportProductVO> exportProduct(ProductSearchExportDTO productSearchDTO);

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/8/27
     * @Descripate 批量修改机构通道优先级
     **/
    int batchUpdateSort(String modifier, List<BatchUpdateSortDTO> batchUpdateSortList);

    /**
     * 导出机构通道
     * @param searchChannelExportDTO
     * @return
     */
    List<ProductChannelVO> exportProductChannel(SearchChannelExportDTO searchChannelExportDTO);
}
