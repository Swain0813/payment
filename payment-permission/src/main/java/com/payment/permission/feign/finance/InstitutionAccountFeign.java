package com.payment.permission.feign.finance;

import com.payment.common.dto.TradeCheckAccountDTO;
import com.payment.common.dto.TradeCheckAccountExportDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.vo.ExportTradeAccountVO;
import com.payment.permission.feign.finance.impl.InstitutionAccountFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(value = "asianwallet-finance", fallback = InstitutionAccountFeignImpl.class)
public interface InstitutionAccountFeign {

    /**
     * 分页查询交易对账总表信息
     *
     * @param tradeCheckAccountDTO
     * @return
     */
    @PostMapping("/finance/pageTradeCheckAccount")
    BaseResponse pageTradeCheckAccount(@RequestBody @ApiParam TradeCheckAccountDTO tradeCheckAccountDTO);

    /**
     * 导出交易对账信息
     * @param tradeCheckAccountDTO
     * @return
     */
    @PostMapping("/finance/exportTradeCheckAccount")
    ExportTradeAccountVO exportTradeCheckAccount(@RequestBody @ApiParam TradeCheckAccountExportDTO tradeCheckAccountDTO);
}
