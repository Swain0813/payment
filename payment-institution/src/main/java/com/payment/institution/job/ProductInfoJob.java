package com.payment.institution.job;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.redis.RedisService;
import com.payment.common.utils.IDS;
import com.payment.institution.dao.InstitutionProductAuditMapper;
import com.payment.institution.dao.InstitutionProductHistoryMapper;
import com.payment.institution.dao.InstitutionProductMapper;
import com.payment.institution.entity.InstitutionProduct;
import com.payment.institution.entity.InstitutionProductAudit;
import com.payment.institution.entity.InstitutionProductHistory;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @description: 产品审核生效job
 * @author: YangXu
 * @create: 2019-03-04 17:03
 **/
@Slf4j
@Transactional
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ProductInfoJob implements BaseJob{
    @Autowired
    private InstitutionProductMapper institutionProductMapper;

    @Autowired
    private InstitutionProductHistoryMapper institutionProductHistoryMapper;

    @Autowired
    private InstitutionProductAuditMapper institutionProductAuditMapper;

    @Autowired
    private RedisService redisService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        log.info("---------------------ProductInfoJob--------------------------------------- jobDataMap :{}", jobDataMap);
        if(jobDataMap.get("insProductId") == null) return;

        String insProductId = jobDataMap.get("insProductId").toString();

        InstitutionProductAudit oldInstitutionProductAudit = institutionProductAuditMapper.selectByPrimaryKey(insProductId);
        InstitutionProduct oldInstitutionProduct = institutionProductMapper.selectByPrimaryKey(insProductId);
        //将原纪录移动到历史表
        InstitutionProductHistory institutionProductHistory = new InstitutionProductHistory();
        BeanUtils.copyProperties(oldInstitutionProduct, institutionProductHistory);
        institutionProductHistory.setId(IDS.uuid2());
        institutionProductHistory.setInsProId(oldInstitutionProduct.getId());
        institutionProductHistory.setCreateTime(oldInstitutionProduct.getCreateTime());
        institutionProductHistory.setCreator(oldInstitutionProduct.getCreator());
        institutionProductHistory.setModifier(oldInstitutionProduct.getModifier());
        institutionProductHistory.setUpdateTime(oldInstitutionProduct.getUpdateTime());
        institutionProductHistoryMapper.insert(institutionProductHistory);

        //将审核表信息更新到主表
        InstitutionProduct institutionProduct = new InstitutionProduct();
        institutionProduct.setId(oldInstitutionProductAudit.getId());
        institutionProduct.setEnabled(oldInstitutionProductAudit.getEnabled());
        institutionProduct.setFloatRate(oldInstitutionProductAudit.getFloatRate());
        institutionProduct.setRateType(oldInstitutionProductAudit.getRateType());
        institutionProduct.setRate(oldInstitutionProductAudit.getRate());
        institutionProduct.setFeePayer(oldInstitutionProductAudit.getFeePayer());
        institutionProduct.setRefundRateType(oldInstitutionProductAudit.getRefundRateType());//退款费率类型
        institutionProduct.setRefundRate(oldInstitutionProductAudit.getRefundRate());//退款费率
        institutionProduct.setRefundMinTate(oldInstitutionProductAudit.getRefundMinTate());//退款手续费最大值
        institutionProduct.setRefundMaxTate(oldInstitutionProductAudit.getRefundMaxTate());//退款手续费最小值
        institutionProduct.setRefundAddValue(oldInstitutionProductAudit.getRefundAddValue());//退款附加值
        institutionProduct.setMinTate(oldInstitutionProductAudit.getMinTate());
        institutionProduct.setMaxTate(oldInstitutionProductAudit.getMaxTate());
        institutionProduct.setSettleCycle(oldInstitutionProductAudit.getSettleCycle());
        institutionProduct.setAddValue(oldInstitutionProductAudit.getAddValue());
        institutionProduct.setModifier(oldInstitutionProductAudit.getModifier());
        institutionProduct.setCreateTime(oldInstitutionProduct.getCreateTime());
        institutionProduct.setCreator(oldInstitutionProduct.getCreator());
        institutionProduct.setUpdateTime(new Date());
        institutionProduct.setAuditInfoStatus(TradeConstant.AUDIT_SUCCESS);
        institutionProductMapper.updateByPrimaryKeySelective(institutionProduct);


        //若审核表限额状态为成功，删除审核表记录
        if(TradeConstant.AUDIT_SUCCESS.equals(oldInstitutionProductAudit.getAuditLimitStatus())){
            institutionProductAuditMapper.deleteByPrimaryKey(insProductId);
        }
        //审核通过后将新增和修改的机构产品信息添加的redis里
        try{
            redisService.set(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(oldInstitutionProductAudit.getInstitutionId().concat("_").concat(oldInstitutionProductAudit.getProductId())),
                    JSON.toJSONString(institutionProductMapper.selectByPrimaryKey(insProductId)));
        }catch (Exception e){
            log.error("审核通过后将新增和修改的机构产品信息添加的redis里："+e.getMessage());
        }
    }
}
