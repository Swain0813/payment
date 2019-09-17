package com.payment.clearing.service.impl;

import com.alibaba.fastjson.JSON;
import com.payment.clearing.dao.InstitutionMapper;
import com.payment.clearing.service.CommonService;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.entity.Institution;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.EResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 共通方法的实现
 */
@Slf4j
@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private RedisService redisService;


    @Autowired
    private InstitutionMapper institutionMapper;

    /**
     * 获得机构信息从redis里获取
     * @param institutionCode
     * @return
     */
    @Override
    public Institution getInstitutionInfo(String institutionCode) {
        //查询机构信息,先从redis获取
        Institution institution = JSON.parseObject(redisService.get(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institutionCode)), Institution.class);
        if (institution == null) {
            //redis不存在,从数据库获取
            institution = institutionMapper.selectByInstitutionCode(institutionCode);
            if (institution == null || !institution.getEnabled()) {
                log.info("-----------------清结算服务机构信息不存在 -----------------  institutionCode :{}", institutionCode);
                //机构信息不存在
                throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());
            }
            //同步redis
            redisService.set(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institution.getInstitutionCode()), JSON.toJSONString(institution));
        }
        return institution;
    }
}
