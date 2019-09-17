package com.payment.permission.feign.institution;

import com.payment.common.dto.*;
import com.payment.common.entity.Product;
import com.payment.common.response.BaseResponse;
import com.payment.common.vo.ExportProductVO;
import com.payment.permission.feign.institution.impl.ProductFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@FeignClient(value = "payment-institution", fallback = ProductFeignImpl.class)
public interface ProductFeign {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 添加产品信息
     **/
    @PostMapping("/product/addProduct")
    BaseResponse addProduct(@RequestBody @ApiParam Product product);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 更新产品
     **/
    @PostMapping("/product/updateProduct")
    BaseResponse updateProduct(@RequestBody @ApiParam Product product);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 查询所有产品
     **/
    @GetMapping("/product/selectProduct")
    BaseResponse selectProduct(@RequestBody @ApiParam ProductSearchDTO productSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询产品
     **/
    @GetMapping("/product/pageProduct")
    BaseResponse pageProduct(@RequestBody @ApiParam ProductSearchDTO productSearchDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 根据支付方式查询所有产品
     **/
    @GetMapping("/product/selectProductByPayType")
    BaseResponse selectProductByPayType(@RequestParam("payType") @ApiParam String payType);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 机构添加产品信息
     **/
    @PostMapping("/product/addInstitutionProduct")
    BaseResponse addInstitutionProduct(@RequestBody @ApiParam List<InstitutionProductDTO> institutionProductDtos);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构产品信息
     **/
    @PostMapping("/product/pageFindInsProduct")
    BaseResponse pageFindInsProduct(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 导出机构产品信息
     **/
    @PostMapping("/product/exportInsProduct")
    BaseResponse exportInsProduct(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构产品审核信息
     **/
    @PostMapping("/product/pageFindInsProductAudit")
    BaseResponse pageFindInsProductAudit(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 根据产品Id查询产品详情
     **/
    @GetMapping("/product/getInsProductById")
    BaseResponse getInsProductById(@RequestParam("insProductId") @ApiParam String insProductId);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 根据产品Id查询产品审核详情
     **/
    @GetMapping("/product/getInsProductAuditById")
    BaseResponse getInsProductAuditById(@RequestParam("insProductId") @ApiParam String insProductId);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 根据机构编码查询产品通道
     **/
    @GetMapping("/product/getProChannelByInstitutionCode")
    BaseResponse getProChannelByInstitutionCode(@RequestParam("institutionCode") @ApiParam String institutionCode);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 根据机构编码查询产品通道
     **/
    @GetMapping("/product/selectProductByInsCode")
    BaseResponse selectProductByInsCode(@RequestParam("institutionCode") @ApiParam String institutionCode);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 修改产品信息
     **/
    @PostMapping("/product/updateInfoProduct")
    BaseResponse updateInfoProduct(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 启用禁用机构产品
     **/
    @PostMapping("/product/updateProductEnable")
    BaseResponse updateProductEnable(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 批量审核产品信息
     **/
    @PostMapping("/product/auditInfoProduct")
    BaseResponse auditInfoProduct(@RequestBody @ApiParam AuaditProductDTO auaditProductDTO);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 修改产品限额
     **/
    @PostMapping("/product/updateLimitProduct")
    BaseResponse updateLimitProduct(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 批量审核产品限额
     **/
    @PostMapping("/product/auditLimitProduct")
    BaseResponse auditLimitProduct(@RequestBody @ApiParam AuaditProductDTO auaditProductDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 批量审核产品限额
     **/
    @PostMapping("/product/allotProductChannel")
    BaseResponse allotProductChannel(@RequestBody @ApiParam @Valid InstProdDTO instProdDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询产品通道管理信息
     **/
    @PostMapping("/product/pageFindProductChannel")
    BaseResponse pageFindProductChannel(@RequestBody @ApiParam SearchChannelDTO searchChannelDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 删除产品通道管理信息
     **/
    @PostMapping("/product/deleteProductChannel")
    BaseResponse deleteProductChannel(@RequestBody @ApiParam String insChaId);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 启用禁用产品通道
     **/
    @GetMapping("/product/banProductChannel")
    BaseResponse banProductChannel(@RequestParam("insChaId") @ApiParam String insChaId, @RequestParam("enabled") @ApiParam Boolean enabled);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 修改机构通道优先级
     **/
    @GetMapping("/product/updateSort")
    BaseResponse updateSort(@RequestParam("insChaId") @ApiParam String insChaId, @RequestParam("sort") @ApiParam String sort, @RequestParam("enabled") @ApiParam boolean enabled);


    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/5/24
     * @Descripate 导出产品信息
     **/
    @PostMapping("/product/exportProduct")
    List<ExportProductVO> exportProduct(ProductSearchExportDTO productSearchDTO);

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/5/24
     * @Descripate 批量修改机构通道优先级
     **/
    @PostMapping("/product/batchUpdateSort")
    BaseResponse batchUpdateSort(List<BatchUpdateSortDTO> batchUpdateSortList);

    /**
     * 导出机构通道
     *
     * @param searchChannelExportDTO
     * @return
     */
    @PostMapping("/product/exportProductChannel")
    BaseResponse exportProductChannel(SearchChannelExportDTO searchChannelExportDTO);
}
