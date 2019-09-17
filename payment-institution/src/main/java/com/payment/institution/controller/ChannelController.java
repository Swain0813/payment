package com.payment.institution.controller;
import com.payment.common.base.BaseController;
import com.payment.common.dto.ChannelDTO;
import com.payment.common.dto.ChannelExportDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.institution.service.ChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-30 14:22
 **/
@RestController
@Api(description ="通道接口")
@RequestMapping("/channel")
public class ChannelController extends BaseController {

    @Autowired
    private ChannelService channelService;

    @ApiOperation(value = "添加通道信息")
    @PostMapping("/addChannel")
    public BaseResponse addChannel(@RequestBody @ApiParam ChannelDTO channelDTO) {
        return ResultUtil.success(channelService.addChannel(this.getSysUserVO().getUsername(),channelDTO));
    }

    @ApiOperation(value = "修改通道信息")
    @PostMapping("/updateChannel")
    public BaseResponse updateChannel(@RequestBody @ApiParam ChannelDTO channelDTO) {
        return ResultUtil.success(channelService.updateChannel(this.getSysUserVO().getUsername(),channelDTO));
    }

    @ApiOperation(value = "分页查询通道信息")
    @PostMapping("/pageFindChannel")
    public BaseResponse pageFindChannel(@RequestBody @ApiParam ChannelDTO channelDTO) {
        if (StringUtils.isBlank(channelDTO.getLanguage())) {
            channelDTO.setLanguage(this.getLanguage());
        }
        return ResultUtil.success(channelService.pageFindChannel(channelDTO));
    }
    @ApiOperation(value = "查询所有通道信息")
    @PostMapping("/getAllChannel")
    public BaseResponse getAllChannel() {
        return ResultUtil.success(channelService.getAllChannel());
    }


    @ApiOperation(value = "启用禁用通道")
    @GetMapping("/banChannel")
    public BaseResponse banChannel(@RequestParam @ApiParam String channelId,@RequestParam @ApiParam Boolean enabled) {
        return ResultUtil.success(channelService.banChannel(this.getSysUserVO().getUsername(),channelId,enabled));
    }


    @ApiOperation(value = "根据通道id查取详情")
    @GetMapping("/getChannelById")
    public BaseResponse getChannelById(@RequestParam @ApiParam String channelId) {
        return ResultUtil.success(channelService.getChannelById(channelId,this.getLanguage()));
    }

    @ApiOperation(value = "根据产品id查取通道")
    @GetMapping("/getChannelByProductId")
    public BaseResponse getChannelByProductId(@RequestParam @ApiParam String productId) {
        return ResultUtil.success(channelService.getChannelByProductId(productId));
    }

    @ApiOperation(value = "根据机构Id和产品Id查询未添加通道")
    @GetMapping("/getChannelByInsIdAndProId")
    public BaseResponse getChannelByInsIdAndProId(@RequestParam @ApiParam String institutionId,@RequestParam @ApiParam String productId) {
        return ResultUtil.success(channelService.getChannelByInsIdAndProId(institutionId,productId));
    }

    @ApiOperation(value = "通道导出功能")
    @PostMapping("/exportAllChannels")
    public BaseResponse exportAllChannels(@RequestBody @ApiParam ChannelExportDTO channelDTO) {
        return ResultUtil.success(channelService.exportAllChannels(channelDTO));
    }
}
