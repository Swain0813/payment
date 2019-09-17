package com.payment.permission.controller;

import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.*;
import com.payment.common.entity.Product;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.common.vo.*;
import com.payment.permission.feign.institution.ProductFeign;
import com.payment.permission.service.InstitutionFeignService;
import com.payment.permission.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-29 16:27
 **/
@RestController
@Api(description = "产品相关接口")
@RequestMapping("/product")
public class ProductFeignController extends BaseController {

    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private ProductFeign productFeign;
    @Autowired
    private InstitutionFeignService institutionFeignService;

    @ApiOperation(value = "添加产品信息")
    @PostMapping("/addProduct")
    public BaseResponse addProduct(@RequestBody @ApiParam Product product) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(product),
                "添加产品信息"));
        return productFeign.addProduct(product);
    }

    @ApiOperation(value = "更新产品")
    @PostMapping("/updateProduct")
    public BaseResponse updateProduct(@RequestBody @ApiParam Product product) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(product),
                "更新产品"));
        return productFeign.updateProduct(product);
    }

    @ApiOperation(value = "查询所有产品")
    @PostMapping("/selectProduct")
    public BaseResponse selectProduct(@RequestBody @ApiParam ProductSearchDTO productSearchDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, null,
                "查询所有产品"));
        return productFeign.selectProduct(productSearchDTO);
    }

    @ApiOperation(value = "分页查询产品")
    @PostMapping("/pageProduct")
    public BaseResponse pageProduct(@RequestBody @ApiParam ProductSearchDTO productSearchDTO) {
        return productFeign.pageProduct(productSearchDTO);
    }

    @ApiOperation(value = "根据支付方式查询所有产品")
    @GetMapping("/selectProductByPayType")
    public BaseResponse selectProductByPayType(@RequestParam @ApiParam String payType) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(this.getRequest().getParameterMap()),
                "根据支付方式查询所有产品"));
        return productFeign.selectProductByPayType(payType);
    }


    @ApiOperation(value = "机构添加产品信息")
    @PostMapping("/addInstitutionProduct")
    public BaseResponse addInstitutionProduct(@RequestBody @ApiParam List<InstitutionProductDTO> institutionProductDtos) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(institutionProductDtos),
                "机构添加产品信息"));
        return productFeign.addInstitutionProduct(institutionProductDtos);
    }

    @ApiOperation(value = "分页查询机构产品信息")
    @PostMapping("/pageFindInsProduct")
    public BaseResponse pageFindInsProduct(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(institutionProductDto),
                "分页查询机构产品信息"));
        return productFeign.pageFindInsProduct(institutionProductDto);
    }

    @ApiOperation(value = "导出机构产品信息")
    @PostMapping("/exportInsProductInfo")
    public BaseResponse exportInsProduct(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(institutionProductDto),
                "导出机构产品信息"));
        BaseResponse baseResponse = productFeign.exportInsProduct(institutionProductDto);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<InsProExportVO> insProList = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            insProList.add(JSON.parseObject(JSON.toJSONString(datum), InsProExportVO.class));
        }
        ExcelWriter writer = null;
        try {
            writer = institutionFeignService.getInsproExcelWriter(insProList, InsProExportVO.class);
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }

    @ApiOperation(value = "导出产品信息")
    @PostMapping("/exportProduct")
    public BaseResponse exportProduct(@RequestBody @ApiParam ProductSearchExportDTO productSearchDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(productSearchDTO),
                "导出产品信息"));
        List<ExportProductVO> list = productFeign.exportProduct(productSearchDTO);
        if (list.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ExcelWriter writer = null;
        try {
            writer = institutionFeignService.getProductExcelWriter(list, ExportProductVO.class);
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }

    @ApiOperation(value = "导出机构产品限额信息")
    @PostMapping("/exportInsProductLimit")
    public BaseResponse exportInsProductLimt(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(institutionProductDto),
                "导出机构产品信息"));
        BaseResponse baseResponse = productFeign.exportInsProduct(institutionProductDto);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<InsProExportLimitVO> insProList = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            insProList.add(JSON.parseObject(JSON.toJSONString(datum), InsProExportLimitVO.class));
        }
        ExcelWriter writer = null;
        try {
            writer = institutionFeignService.getInsproLimitExcelWriter(insProList, InsProExportLimitVO.class);
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return ResultUtil.success();
    }


    @ApiOperation(value = "分页查询机构产品审核信息")
    @PostMapping("/pageFindInsProductAudit")
    public BaseResponse pageFindInsProductAudit(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(institutionProductDto),
                "分页查询机构产品审核信息"));
        return productFeign.pageFindInsProductAudit(institutionProductDto);
    }

    @ApiOperation(value = "根据机构产品Id查询产品详情")
    @GetMapping("/getInsProductById")
    public BaseResponse getInsProductById(@RequestParam @ApiParam String insProductId) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "根据产品Id查询产品详情"));
        return productFeign.getInsProductById(insProductId);
    }

    @ApiOperation(value = "根据机构产品Id查询产品审核详情")
    @GetMapping("/getInsProductAuditById")
    public BaseResponse getInsProductAuditById(@RequestParam @ApiParam String insProductId) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "根据产品Id查询产品审核详情"));
        return productFeign.getInsProductAuditById(insProductId);
    }

    @ApiOperation(value = "根据机构编码查询产品通道")
    @GetMapping("/getProChannelByInstitutionCode")
    public BaseResponse getProChannelByInstitutionCode(@RequestParam @ApiParam String institutionCode) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "根据机构编码查询产品通道"));
        return productFeign.getProChannelByInstitutionCode(institutionCode);
    }

    @ApiOperation(value = "根据机构号Code查询所有产品")
    @GetMapping("/selectProductByInsCode")
    public BaseResponse selectProductByInsCode(@RequestParam @ApiParam String institutionCode) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "根据机构号Code查询所有产品"));
        return productFeign.selectProductByInsCode(institutionCode);
    }


    @ApiOperation(value = "修改产品信息")
    @PostMapping("/updateInfoProduct")
    public BaseResponse updateInfoProduct(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(institutionProductDto),
                "修改产品信息"));
        return productFeign.updateInfoProduct(institutionProductDto);
    }

    @ApiOperation(value = "批量审核产品信息")
    @PostMapping("/auditInfoProduct")
    public BaseResponse auditInfoProduct(@RequestBody @ApiParam AuaditProductDTO auaditProductDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(auaditProductDTO),
                "批量审核产品信息"));
        return productFeign.auditInfoProduct(auaditProductDTO);
    }

    @ApiOperation(value = "启用禁用机构产品")
    @PostMapping("/updateProductEnable")
    public BaseResponse updateProductEnable(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(institutionProductDto),
                "启用禁用机构产品"));
        return productFeign.updateProductEnable(institutionProductDto);
    }

    @ApiOperation(value = "修改产品限额")
    @PostMapping("/updateLimitProduct")
    public BaseResponse updateLimitProduct(@RequestBody @ApiParam InstitutionProductDTO institutionProductDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(institutionProductDto),
                "修改产品限额"));
        return productFeign.updateLimitProduct(institutionProductDto);
    }

    @ApiOperation(value = "批量审核产品限额")
    @PostMapping("/auditLimitProduct")
    public BaseResponse auditLimitProduct(@RequestBody @ApiParam AuaditProductDTO auaditProductDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(auaditProductDTO),
                "批量审核产品限额"));
        return productFeign.auditLimitProduct(auaditProductDTO);
    }

    @ApiOperation(value = "机构产品分配通道")
    @PostMapping("/allotProductChannel")
    public BaseResponse allotProductChannel(@RequestBody @ApiParam @Valid InstProdDTO instProdDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(instProdDTO),
                "机构产品分配通道"));
        return productFeign.allotProductChannel(instProdDTO);
    }

    @ApiOperation(value = "分页查询产品通道管理信息")
    @PostMapping("/pageFindProductChannel")
    public BaseResponse pageFindProductChannel(@RequestBody @ApiParam SearchChannelDTO searchChannelDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(searchChannelDTO),
                "分页查询产品通道管理信息"));
        return productFeign.pageFindProductChannel(searchChannelDTO);
    }

    @ApiOperation(value = "删除产品通道管理信息")
    @GetMapping("/deleteProductChannel")
    public BaseResponse deleteProductChannel(@RequestBody @ApiParam String insChaId) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(this.getRequest().getParameterMap()),
                "删除产品通道管理信息"));
        return productFeign.deleteProductChannel(insChaId);
    }

    @ApiOperation(value = "启用禁用产品通道")
    @GetMapping("/banProductChannel")
    public BaseResponse banProductChannel(@RequestParam @ApiParam String insChaId, @RequestParam @ApiParam Boolean enabled) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(this.getRequest().getParameterMap()),
                "启用禁用产品通道"));
        return productFeign.banProductChannel(insChaId, enabled);
    }

    @ApiOperation(value = "修改机构通道优先级")
    @GetMapping("/updateSort")
    public BaseResponse updateSort(@RequestParam @ApiParam String insChaId, @RequestParam @ApiParam String sort, @RequestParam @ApiParam boolean enabled) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(this.getRequest().getParameterMap()),
                "修改机构通道优先级"));
        return productFeign.updateSort(insChaId, sort, enabled);
    }

    @ApiOperation(value = "批量修改机构通道优先级")
    @PostMapping("/batchUpdateSort")
    public BaseResponse batchUpdateSort(@RequestBody @ApiParam List<BatchUpdateSortDTO> batchUpdateSortList) {
        return productFeign.batchUpdateSort(batchUpdateSortList);
    }

    @ApiOperation(value = "导出机构通道管理信息")
    @PostMapping("/exportProductChannel")
    public BaseResponse exportProductChannel(@RequestBody @ApiParam SearchChannelExportDTO productSearchDTO) {
        BaseResponse baseResponse = productFeign.exportProductChannel(productSearchDTO);
        ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) baseResponse.getData();
        if (data == null || data.size() == 0) {//数据不存在的场合
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        ArrayList<ProductChannelVO> productChannelVOS = new ArrayList<>();
        for (LinkedHashMap datum : data) {
            productChannelVOS.add(JSON.parseObject(JSON.toJSONString(datum), ProductChannelVO.class));
        }
        ExcelWriter writer = null;
        try {
            writer = institutionFeignService.getProductChannelExcelWriter(productChannelVOS, ProductChannelExportVO.class);
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.INSTITUTION_INFORMATION_EXPORT_FAILED.getCode());
        } finally {
            writer.close();
        }
        return ResultUtil.success();
    }
}
