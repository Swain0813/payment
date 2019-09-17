package com.payment.permission.feign.institution;

import com.payment.common.dto.*;
import com.payment.common.entity.Bank;
import com.payment.common.entity.BankIssuerid;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.impl.ChannelFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(value = "payment-institution", fallback = ChannelFeignImpl.class)
public interface ChannelFeign {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 添加通道信息
     **/
    @PostMapping("/channel/addChannel")
    BaseResponse addChannel(@RequestBody @ApiParam ChannelDTO channelDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 修改通道信息
     **/
    @PostMapping("/channel/updateChannel")
    BaseResponse updateChannel(@RequestBody @ApiParam ChannelDTO channelDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 分页查询通道信息
     **/
    @PostMapping("/channel/pageFindChannel")
    BaseResponse pageFindChannel(@RequestBody @ApiParam ChannelDTO channelDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 查询所有通道信息
     **/
    @PostMapping("/channel/getAllChannel")
    BaseResponse getAllChannel();

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 分页查询产品通道管理信息
     **/
    @PostMapping("/channel/pageFindProductChannel")
    BaseResponse pageFindProductChannel(@RequestBody @ApiParam SearchChannelDTO searchChannelDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 启用禁用通道
     **/
    @GetMapping("/channel/banChannel")
    BaseResponse banChannel(@RequestParam("channelId") @ApiParam String channelId, @RequestParam("enabled") @ApiParam Boolean enabled);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 根据通道id查取详情
     **/
    @GetMapping("/channel/getChannelById")
    BaseResponse getChannelById(@RequestParam("channelId") @ApiParam String channelId);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 根据产品id查取通道
     **/
    @GetMapping("/channel/getChannelByProductId")
    BaseResponse getChannelByProductId(@RequestParam("productId") @ApiParam String productId);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 根据机构Id和产品Id查询未添加通道
     **/
    @GetMapping("/channel/getChannelByInsIdAndProId")
    BaseResponse getChannelByInsIdAndProId(@RequestParam("institutionId") @ApiParam String institutionId, @RequestParam("productId") @ApiParam String productId);


    /**
     * 通道导出功能
     *
     * @param channelDTO
     * @return
     */
    @PostMapping("/channel/exportAllChannels")
    BaseResponse exportAllChannels(@RequestBody @ApiParam ChannelDTO channelDTO);

    /**
     * 配置银行issureid对照信息
     *
     * @param bankIssuerid
     * @return
     */
    @PostMapping("/bankcard/addBankIssureId")
    BaseResponse addBankIssureId(@RequestBody @ApiParam List<BankIssuerid> bankIssuerid);

    /**
     * 修改银行issureid对照信息
     *
     * @param bankIssuerid
     * @return
     */
    @PostMapping("/bankcard/updateBankIssureId")
    BaseResponse updateBankIssureId(@RequestBody @ApiParam BankIssuerid bankIssuerid);

    /**
     * 查询银行issureid对照信息
     *
     * @param bankIssuerid
     * @return
     */
    @PostMapping("/bankcard/pageFindBankIssuerid")
    BaseResponse pageFindBankIssuerid(@RequestBody @ApiParam BankIssueridDTO bankIssuerid);

    /**
     * 导出银行issureid对照信息
     *
     * @param bankIssuerid
     * @return
     */
    @PostMapping("/bankcard/exportBankIssuerid")
    BaseResponse exportBankIssuerid(@RequestBody @ApiParam BankIssueridExportDTO bankIssuerid);

    /**
     * 配置银行信息
     *
     * @param bankIssuerid
     * @return
     */
    @PostMapping("/bankcard/addBank")
    BaseResponse addBank(@RequestBody @ApiParam Bank bankIssuerid);

    /**
     * 修改银行信息
     *
     * @param bankIssuerid
     * @return
     */
    @PostMapping("/bankcard/updateBank")
    BaseResponse updateBank(@RequestBody @ApiParam Bank bankIssuerid);

    /**
     * 查询银行issureid对照信息
     *
     * @param bank
     * @return
     */
    @PostMapping("/bankcard/pageFindBank")
    BaseResponse pageFindBank(@RequestBody @ApiParam BankDTO bank);

}
