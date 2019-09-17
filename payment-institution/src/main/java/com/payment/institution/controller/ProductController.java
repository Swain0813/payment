package com.payment.institution.controller;

import com.payment.common.base.BaseController;
import com.payment.common.dto.*;
import com.payment.common.entity.Product;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.common.vo.ExportProductVO;
import com.payment.institution.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @description: 产品
 * @author: YangXu
 * @create: 2019-01-29 14:14
 **/
@RestController
@Api(description = "产品接口")
@RequestMapping("/product")
public class ProductController extends BaseController {

    @Autowired
    private ProductService productService;

    @ApiOperation(value = "添加产品信息")
    @PostMapping("/addProduct")
    public BaseResponse addProduct(@RequestBody @ApiParam Product product) {
        return ResultUtil.success(productService.addProduct(this.getSysUserVO().getUsername(), product));
    }

    @ApiOperation(value = "更新产品")
    @PostMapping("/updateProduct")
    public BaseResponse updateProduct(@RequestBody @ApiParam Product product) {
        return ResultUtil.success(productService.updateProduct(this.getSysUserVO().getUsername(), product));
    }

    @ApiOperation(value = "分页查询产品")
    @PostMapping("/pageProduct")
    public BaseResponse pageProduct(@RequestBody @ApiParam ProductSearchDTO productSearchDTO) {
        if (StringUtils.isBlank(productSearchDTO.getLanguage())) {
            productSearchDTO.setLanguage(this.getLanguage());
        }
        return ResultUtil.success(productService.pageProduct(productSearchDTO));
    }

    @ApiOperation(value = "导出产品信息")
    @PostMapping("/exportProduct")
    public List<ExportProductVO> exportProduct(@RequestBody @ApiParam ProductSearchExportDTO productSearchDTO) {
        if (StringUtils.isBlank(productSearchDTO.getLanguage())) {
            productSearchDTO.setLanguage(this.getLanguage());
        }
        return productService.exportProduct(productSearchDTO);
    }

    @ApiOperation(value = "查询所有产品")
    @PostMapping("/selectProduct")
    public BaseResponse selectProduct(@RequestBody @ApiParam ProductSearchDTO productSearchDTO) {
        if (StringUtils.isBlank(productSearchDTO.getLanguage())) {
            productSearchDTO.setLanguage(this.getLanguage());
        }
        return ResultUtil.success(productService.selectProduct(productSearchDTO));
    }

    @ApiOperation(value = "根据支付方式查询所有产品")
    @GetMapping("/selectProductByPayType")
    public BaseResponse selectProductByPayType(@RequestParam @ApiParam String payType) {
        return ResultUtil.success(productService.selectProductByPayType(payType, this.getLanguage()));
    }

    @ApiOperation(value = "根据机构号Code查询所有产品")
    @GetMapping("/selectProductByInsCode")
    public BaseResponse selectProductByInsCode(@RequestParam @ApiParam String institutionCode) {
        return ResultUtil.success(productService.selectProductByInsCode(this.getLanguage(), institutionCode));
    }

    @ApiOperation(value = "机构添加产品信息")
    @PostMapping("/addInstitutionProduct")
    public BaseResponse addInstitutionProduct(@RequestBody @ApiParam List<InstitutionProductDTO> institutionProductDtos) {
        return ResultUtil.success(productService.addInstitutionProduct(this.getSysUserVO().getUsername(), institutionProductDtos));
    }

    @ApiOperation(value = "分页查询机构产品信息")
    @PostMapping("/pageFindInsProduct")
    public BaseResponse pageFindInsProduct(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto) {
        if (StringUtils.isBlank(institutionProductDto.getLanguage())) {
            institutionProductDto.setLanguage(this.getLanguage());
        }
        return ResultUtil.success(productService.pageFindInsProduct(institutionProductDto));
    }


    @ApiOperation(value = "导出机构产品信息")
    @PostMapping("/exportInsProduct")
    public BaseResponse exportInsProduct(@RequestBody @ApiParam InstitutionProductExportDTO institutionProductDto) {
        if (StringUtils.isBlank(institutionProductDto.getLanguage())) {
            institutionProductDto.setLanguage(this.getLanguage());
        }
        return ResultUtil.success(productService.exportInsProduct(institutionProductDto));
    }

    @ApiOperation(value = "分页查询机构产品审核信息")
    @PostMapping("/pageFindInsProductAudit")
    public BaseResponse pageFindInsProductAudit(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto) {
        if (StringUtils.isBlank(institutionProductDto.getLanguage())) {
            institutionProductDto.setLanguage(this.getLanguage());
        }
        return ResultUtil.success(productService.pageFindInsProductAudit(institutionProductDto));
    }

    @ApiOperation(value = "根据产品Id查询产品详情")
    @GetMapping("/getInsProductById")
    public BaseResponse getInsProductById(@RequestParam @ApiParam String insProductId) {
        return ResultUtil.success(productService.getInsProductById(insProductId, this.getLanguage()));
    }

    @ApiOperation(value = "根据产品Id查询产品审核详情")
    @GetMapping("/getInsProductAuditById")
    public BaseResponse getInsProductAuditById(@RequestParam @ApiParam String insProductId) {
        return ResultUtil.success(productService.getInsProductAuditById(insProductId, this.getLanguage()));
    }

    @ApiOperation(value = "根据机构编码查询产品通道")
    @GetMapping("/getProChannelByInstitutionCode")
    public BaseResponse getProChannelByInstitutionCode(@RequestParam @ApiParam String institutionCode) {
        return ResultUtil.success(productService.getProChannelByInstitutionCode(institutionCode, this.getLanguage()));
    }

    @ApiOperation(value = "修改产品信息")
    @PostMapping("/updateInfoProduct")
    public BaseResponse updateInfoProduct(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto) {
        return ResultUtil.success(productService.updateInfoProduct(this.getSysUserVO().getUsername(), institutionProductDto));
    }

    @ApiOperation(value = "修改产品限额")
    @PostMapping("/updateLimitProduct")
    public BaseResponse updateLimitProduct(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto) {
        return ResultUtil.success(productService.updateLimitProduct(this.getSysUserVO().getUsername(), institutionProductDto));
    }

    @ApiOperation(value = "启用禁用机构产品")
    @PostMapping("/updateProductEnable")
    public BaseResponse updateProductEnable(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto) {
        return ResultUtil.success(productService.updateProductEnable(this.getSysUserVO().getUsername(), institutionProductDto));
    }


    @ApiOperation(value = "批量审核产品信息")
    @PostMapping("/auditInfoProduct")
    public BaseResponse auditInfoProduct(@RequestBody @ApiParam AuaditProductDTO auaditProductDTO) {
        BaseResponse baseResponse = productService.auditInfoProduct(this.getSysUserVO().getUsername(), auaditProductDTO);
        String code = baseResponse.getCode();//业务返回码
        if (org.springframework.util.StringUtils.isEmpty(code)) {
            baseResponse.setCode(EResultEnum.SUCCESS.getCode());
            baseResponse.setMsg("SUCCESS");
            return baseResponse;
        }
        return ResultUtil.error(code, this.getErrorMsgMap(code));
    }

    @ApiOperation(value = "批量审核产品限额")
    @PostMapping("/auditLimitProduct")
    public BaseResponse auditLimitProduct(@RequestBody @ApiParam AuaditProductDTO auaditProductDTO) {
        return ResultUtil.success(productService.auditLimitProduct(this.getSysUserVO().getUsername(), auaditProductDTO));
    }

    @ApiOperation(value = "机构产品分配通道")
    @PostMapping("/allotProductChannel")
    public BaseResponse allotProductChannel(@RequestBody @ApiParam @Valid InstProdDTO instProdDTO) {
        return ResultUtil.success(productService.allotProductChannel(this.getSysUserVO().getUsername(), instProdDTO));
    }

    @ApiOperation(value = "分页查询产品通道管理信息")
    @PostMapping("/pageFindProductChannel")
    public BaseResponse pageFindProductChannel(@RequestBody @ApiParam SearchChannelDTO searchChannelDTO) {
        if (StringUtils.isBlank(searchChannelDTO.getLanguage())) {
            searchChannelDTO.setLanguage(this.getLanguage());
        }
        return ResultUtil.success(productService.pageFindProductChannel(searchChannelDTO));
    }

    @ApiOperation(value = "删除产品通道管理信息")
    @GetMapping("/deleteProductChannel")
    public BaseResponse deleteProductChannel(@RequestParam @ApiParam String insChaId) {
        return ResultUtil.success(productService.deleteProductChannel(insChaId));
    }

    @ApiOperation(value = "启用禁用产品通道")
    @GetMapping("/banProductChannel")
    public BaseResponse banProductChannel(@RequestParam @ApiParam String insChaId, @RequestParam @ApiParam Boolean enabled) {
        return ResultUtil.success(productService.banProductChannel(insChaId, enabled));
    }

    @ApiOperation(value = "修改机构通道优先级")
    @GetMapping("/updateSort")
    public BaseResponse updateSort(@RequestParam @ApiParam String insChaId, @RequestParam @ApiParam String sort, @RequestParam @ApiParam boolean enabled) {
        return ResultUtil.success(productService.updateSort(this.getSysUserVO().getUsername(), insChaId, sort, enabled));
    }

    @ApiOperation(value = "批量修改机构通道优先级")
    @PostMapping("/batchUpdateSort")
    public BaseResponse batchUpdateSort(@RequestBody @ApiParam List<BatchUpdateSortDTO> batchUpdateSortList) {
        return ResultUtil.success(productService.batchUpdateSort(this.getSysUserVO().getUsername(), batchUpdateSortList));
    }

    @ApiOperation(value = "导出机构通道管理信息")
    @PostMapping("/exportProductChannel")
    public BaseResponse exportProductChannel(@RequestBody @ApiParam SearchChannelExportDTO searchChannelExportDTO) {
        if (StringUtils.isBlank(searchChannelExportDTO.getLanguage())) {
            searchChannelExportDTO.setLanguage(this.getLanguage());
        }
        return ResultUtil.success(productService.exportProductChannel(searchChannelExportDTO));
    }
}
