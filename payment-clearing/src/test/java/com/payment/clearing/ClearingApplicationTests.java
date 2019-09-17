package com.payment.clearing;
import com.payment.clearing.dao.AccountMapper;
import com.payment.clearing.dao.TcsCtFlowMapper;
import com.payment.clearing.service.ClearService;
import com.payment.clearing.service.SettleService;
import com.payment.clearing.service.ShareBenefitService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClearingApplicationTests {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private TcsCtFlowMapper tcsCtFlowMapper;

    @Autowired
    private ClearService clearService;

    @Autowired
    private SettleService settleService;

    @Autowired
    private ShareBenefitService shareBenefitService;

    @Test
    public void clear() {
        shareBenefitService.ShareBenefitForBatch();
    }
    @Test
    public void  settle() {
        clearService.ClearForGroupBatch();
        settleService.SettlementForBatch();
    }

}
