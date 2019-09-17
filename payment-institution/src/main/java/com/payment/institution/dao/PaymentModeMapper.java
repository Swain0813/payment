package com.payment.institution.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.dto.PaymentModeDTO;
import com.payment.common.vo.PaymentModeVO;
import com.payment.institution.entity.PaymentMode;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 支付方式管理mapper
 */
@Repository
public interface PaymentModeMapper extends BaseMapper<PaymentMode> {
    /**
     * 查询支付信息
     *
     * @param paymentModeDTO
     * @return
     */
    List<PaymentModeVO> pagePayInfo(PaymentModeDTO paymentModeDTO);
    /**
     * 查询所有支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    List<PaymentModeVO> getPayInfo(PaymentModeDTO paymentModeDTO);

    /**
     * 根据payType与dealType
     *
     * @param payType
     * @param dealType
     * @return
     */
    int selectExist(@Param("payType") String payType, @Param("dealType") String dealType);
}
