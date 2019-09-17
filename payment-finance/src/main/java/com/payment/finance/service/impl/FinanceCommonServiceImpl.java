package com.payment.finance.service.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.entity.Institution;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.EResultEnum;
import com.payment.finance.dao.InstitutionMapper;
import com.payment.finance.service.FinanceCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 对账用的共通模块的实现类
 */
@Slf4j
@Service
public class FinanceCommonServiceImpl implements FinanceCommonService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private InstitutionMapper institutionMapper;

    /**
     * 根据机构code获取机构名称
     *
     * @param institutionCode
     * @return
     */
    @Override
    public Institution getInstitutionInfo(String institutionCode) {
        //查询机构信息,先从redis获取
        Institution institution = JSON.parseObject(redisService.get(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institutionCode)), Institution.class);
        if (institution == null) {
            //redis不存在,从数据库获取
            institution = institutionMapper.selectByCode(institutionCode);
            if (institution == null) {
                //机构信息不存在
                throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());
            }
            //同步redis
            redisService.set(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institution.getInstitutionCode()), JSON.toJSONString(institution));
        }
        return institution;
    }
}
