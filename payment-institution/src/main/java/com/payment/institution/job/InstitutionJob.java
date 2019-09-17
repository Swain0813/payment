package com.payment.institution.job;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.entity.Institution;
import com.payment.common.redis.RedisService;
import com.payment.common.utils.IDS;
import com.payment.institution.dao.InstitutionAuditMapper;
import com.payment.institution.dao.InstitutionHistoryMapper;
import com.payment.institution.dao.InstitutionMapper;
import com.payment.institution.entity.InstitutionAudit;
import com.payment.institution.entity.InstitutionHistory;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 设置机构生效的job
 */
@Slf4j
@Transactional
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class InstitutionJob implements BaseJob {

    @Autowired
    private InstitutionMapper institutionMapper;

    @Autowired
    private InstitutionAuditMapper institutionAuditMapper;

    @Autowired
    private InstitutionHistoryMapper institutionHistoryMapper;

    @Autowired
    private RedisService redisService;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        log.info("-------------------InstitutionJob--------------------------------- :jobDataMap :{}", jobDataMap);
        if(jobDataMap.get("institutionId") == null) return;

        String institutionId = jobDataMap.get("institutionId").toString();
        //查询主题表原机构信息.把原机构信息存放历史表
        Institution oleInstitution = institutionMapper.selectByPrimaryKey(institutionId);
        InstitutionHistory institutionHistory = new InstitutionHistory();
        BeanUtils.copyProperties(oleInstitution, institutionHistory);
        institutionHistory.setId(IDS.uuid2());
        institutionHistory.setInstitutionId(institutionId);
        institutionHistoryMapper.insert(institutionHistory);
        institutionMapper.deleteByPrimaryKey(institutionId);
        //将审核表信息移动到主题表
        InstitutionAudit institutionAudit = institutionAuditMapper.selectByPrimaryKey(institutionId);
        Institution institution = new Institution();
        BeanUtils.copyProperties(institutionAudit, institution);
        institution.setAuditStatus(TradeConstant.AUDIT_SUCCESS);
        institution.setUpdateTime(new Date());
        institution.setModifier(institutionAudit.getModifier());
        institution.setEnabled(true);
        institutionMapper.insert(institution);
        institutionAuditMapper.deleteByPrimaryKey(institutionId);
        try{
            //审核通过后将新增和修改的机构信息添加的redis里
            redisService.set(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institutionId), JSON.toJSONString(institution));
        }catch (Exception e){
            log.error("审核通过后将机构信息同步到redis里发生错误："+e.getMessage());
        }
    }
}
