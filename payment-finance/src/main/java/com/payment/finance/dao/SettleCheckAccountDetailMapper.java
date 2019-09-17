package com.payment.finance.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.dto.TradeCheckAccountDTO;
import com.payment.common.dto.TradeCheckAccountSettleExportDTO;
import com.payment.common.entity.SettleCheckAccountDetail;
import com.payment.common.vo.ExportSettleCheckAccountVO;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface SettleCheckAccountDetailMapper extends BaseMapper<SettleCheckAccountDetail> {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/16
     * @Descripate 分页查询机构结算对账详情
     **/
    List<SettleCheckAccountDetail> pageSettleAccountCheckDetail(TradeCheckAccountDTO tradeCheckAccountDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/16
     * @Descripate 导出机构结算对账详情
     **/
    List<ExportSettleCheckAccountVO> exportSettleAccountCheckDetail(TradeCheckAccountSettleExportDTO tradeCheckAccountDTO);
}
