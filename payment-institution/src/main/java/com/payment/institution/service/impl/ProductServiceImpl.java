package com.payment.institution.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.payment.common.base.BaseServiceImpl;
import com.payment.common.config.AuditorProvider;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.*;
import com.payment.common.entity.*;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.ResultUtil;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.IDS;
import com.payment.common.vo.*;
import com.payment.institution.dao.*;
import com.payment.institution.entity.InstitutionProduct;
import com.payment.institution.entity.InstitutionProductAudit;
import com.payment.institution.entity.InstitutionProductHistory;
import com.payment.institution.job.ProductInfoJob;
import com.payment.institution.service.ProductService;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-29 14:16
 **/
@Service
@Slf4j
@Transactional
public class ProductServiceImpl extends BaseServiceImpl<Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private InstitutionProductMapper institutionProductMapper;

    @Autowired
    private AuditorProvider auditorProvider;

    @Autowired
    private InstitutionProductAuditMapper institutionProductAuditMapper;

    @Autowired
    private ProductChannelMapper productChannelMapper;

    @Autowired
    private InstitutionChannelMapper institutionChannelMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private InstitutionMapper institutionMapper;

    @Autowired
    private InstitutionProductHistoryMapper institutionProductHistoryMapper;

    @Autowired
    private QrtzJobDetailsMapper qrtzJobDetailsMapper;

    @Autowired
    private ChannelBankMapper channelBankMapper;

    @Autowired
    private SettleControlMapper settleControlMapper;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 更新产品
     **/
    @Override
    public int updateProduct(String creator, Product product) {
        return productMapper.updateImg(product.getProductCode(), product.getProductImg(), creator, new Date());
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 查询所有产品
     **/
    @Override
    public List<ProductVO> selectProduct(ProductSearchDTO productSearchDTO) {
        return productMapper.getAllProduct(productSearchDTO);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 分页查询产品
     **/
    @Override
    public PageInfo<ProductVO> pageProduct(ProductSearchDTO productSearchDTO) {
        return new PageInfo<ProductVO>(productMapper.pageProduct(productSearchDTO));
    }

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/5/24
     * @Descripate 导出产品信息
     **/
    @Override
    public List<ExportProductVO> exportProduct(ProductSearchExportDTO productSearchDTO) {
        return productMapper.exportProduct(productSearchDTO);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据支付方式查询所有产品
     **/
    @Override
    public List<Product> selectProductByPayType(String payType, String language) {
        //获取当前请求的语言
        language = auditorProvider.getLanguage();//设置语言
        return productMapper.selectProductByPayType(payType, language);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据机构号Code查询所有产品
     **/
    @Override
    public List<InstitutionProductVO> selectProductByInsCode(String language, String institutionCode) {
        //获取当前请求的语言
        language = auditorProvider.getLanguage();//设置语言
        return institutionProductMapper.selectProductByInsCode(language, institutionCode);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 插入产品
     **/
    @Override
    public int addProduct(String creator, Product product) {
        Example example = new Example(Product.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("payType", product.getPayType());
        criteria.andEqualTo("currency", product.getCurrency());
        List<Product> list = productMapper.selectByExample(example);
        if (list.size() > 0) {
            throw new BusinessException(EResultEnum.REPEATED_ADDITION.getCode());
        }
        String id = IDS.uuid2();
        product.setId(id);
        product.setEnabled(true);
        product.setCreator(creator);
        product.setCreateTime(new Date());
        int num = productMapper.insert(product);

        Product p = productMapper.getProductById(id);
        try {
            //审核通过后将新增和修改的机构信息添加的redis里
            redisService.set(AsianWalletConstant.PRODUCT_CACHE_CODE_KEY.concat("_").concat(p.getProductCode().toString()), JSON.toJSONString(p));
            redisService.set(AsianWalletConstant.PRODUCT_CACHE_TYPE_KEY.concat("_").concat(p.getPayType()).concat("_").concat(p.getCurrency()).concat("_").concat(p.getTradeDirection().toString()), JSON.toJSONString(p));
        } catch (Exception e) {
            log.error("将产品信息同步到redis里发生错误：" + e.getMessage());
            throw new BusinessException(EResultEnum.ERROR_REDIS_UPDATE.getCode());
        }
        return num;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 机构添加产品信息
     **/
    @Override
    public String addInstitutionProduct(String creator, List<InstitutionProductDTO> institutionProductDtos) {
        List<InstitutionProduct> list = Lists.newArrayList();
        List<InstitutionProductAudit> listAudit = Lists.newArrayList();
        for (InstitutionProductDTO institutionProductDTO : institutionProductDtos) {
            //查询机构是否已分配产品
            if (institutionProductMapper.selectCountbyInsIdProId(institutionProductDTO.getInstitutionId(), institutionProductDTO.getProductId()) > 0) {
                continue;
            }
            //根据当前机构id获取代理商机构信息
            Institution institution = institutionMapper.selectByPrimaryKey(institutionProductDTO.getInstitutionId());
            if (!StringUtils.isEmpty(institution.getAgencyCode())) {
                //代理商机构信息
                Institution agencyInstitution = institutionMapper.getInstitutionByCode(institution.getAgencyCode());
                InstitutionProduct agencyInstitutionProduct = institutionProductMapper.getInstitutionProductByInstitutionIdAndProductId(agencyInstitution.getId(), institutionProductDTO.getProductId());
                if (agencyInstitutionProduct != null && agencyInstitutionProduct.getRateType().equals(institutionProductDTO.getRateType())) {
                    //单笔定额的场合
                    if (TradeConstant.FEE_TYPE_QUOTA.equals(institutionProductDTO.getRateType())) {
                        if (institutionProductDTO.getRate().compareTo(agencyInstitutionProduct.getRate()) == -1) {
                            throw new BusinessException(EResultEnum.RATE_IS_ILLEGAL.getCode());
                        }
                    } else if (TradeConstant.FEE_TYPE_RATE.equals(institutionProductDTO.getRateType())) {
                        //单笔费率
                        if (institutionProductDTO.getRate().compareTo(agencyInstitutionProduct.getRate()) == -1 ||
                                institutionProductDTO.getMinRate().compareTo(agencyInstitutionProduct.getMinTate()) == -1 ||
                                institutionProductDTO.getMaxRate().compareTo(agencyInstitutionProduct.getMaxTate()) == -1) {
                            throw new BusinessException(EResultEnum.RATE_IS_ILLEGAL.getCode());
                        }
                    }
                }
            }
            String id = IDS.uuid2();
            InstitutionProduct institutionProduct = new InstitutionProduct();
            //判断机构是否添加过产品
            Product product = productMapper.selectByPrimaryKey(institutionProductDTO.getProductId());
            institutionProduct.setId(id);
            institutionProduct.setInstitutionId(institutionProductDTO.getInstitutionId());
            institutionProduct.setProductId(institutionProductDTO.getProductId());
            institutionProduct.setFloatRate(institutionProductDTO.getFloatRate());
            institutionProduct.setRateType(institutionProductDTO.getRateType());
            institutionProduct.setRate(institutionProductDTO.getRate());
            //费率类型为单笔费率的场合才有费率最小值和费率最大值
            if (institutionProductDTO.getRateType() != null && TradeConstant.FEE_TYPE_RATE.equals(institutionProductDTO.getRateType())) {
                institutionProduct.setMaxTate(institutionProductDTO.getMaxRate());//费率最大值
                institutionProduct.setMinTate(institutionProductDTO.getMinRate());//费率最小值
            }
            //退款费率相关字段
            if (institutionProductDTO.getRefundRateType() != null && TradeConstant.FEE_TYPE_RATE.equals(institutionProductDTO.getRefundRateType())) {
                institutionProduct.setRefundMaxTate(institutionProductDTO.getRefundMaxTate());//退款费率最大值
                institutionProduct.setRefundMinTate(institutionProductDTO.getRefundMinTate());//退款费率最小值
            }
            institutionProduct.setRefundAddValue(institutionProductDTO.getRefundAddValue());//退款附加值
            institutionProduct.setRefundRate(institutionProductDTO.getRefundRate());//退款费率
            institutionProduct.setRefundRateType(institutionProductDTO.getRefundRateType());//退款费率类型
            institutionProduct.setDividedMode(institutionProductDTO.getDividedMode());//分润模式
            institutionProduct.setDividedRatio(institutionProductDTO.getDividedRatio());//分润比例
            institutionProduct.setSettleCycle(institutionProductDTO.getSettleCycle());
            institutionProduct.setFeePayer(institutionProductDTO.getFeePayer());
            institutionProduct.setAddValue(institutionProductDTO.getAddValue());
            institutionProduct.setLimitAmount(product.getLimitAmount());
            institutionProduct.setDailyTradingCount(product.getDailyTradingCount());
            institutionProduct.setDailyTotalAmount(product.getDailyTotalAmount());
            institutionProduct.setCreateTime(new Date());
            institutionProduct.setCreator(creator);
            institutionProduct.setEnabled(false);
            institutionProduct.setAuditInfoStatus(TradeConstant.AUDIT_WAIT);
            institutionProduct.setAuditLimitStatus(TradeConstant.AUDIT_SUCCESS);
            InstitutionProductAudit institutionProductAudit = new InstitutionProductAudit();
            BeanUtils.copyProperties(institutionProduct, institutionProductAudit);

            list.add(institutionProduct);
            listAudit.add(institutionProductAudit);
        }
        if (list != null && list.size() > 0) {
            institutionProductAuditMapper.insertList(listAudit);
            institutionProductMapper.insertList(list);
            return "SUCCESS";
        } else {
            throw new BusinessException(EResultEnum.PAYMENTMODE_EXIST.getCode());
        }
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 分页查询机构产品信息
     **/
    @Override
    public PageInfo<InstitutionProductVO> pageFindInsProduct(InstitutionProductDTO institutionProductDto) {
        return new PageInfo<InstitutionProductVO>(institutionProductMapper.pageFindInsProduct(institutionProductDto));
    }

    /**
     * @param institutionProductDto
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 分页查询机构产品信息
     */
    @Override
    public List<InstitutionProductVO> exportInsProduct(InstitutionProductExportDTO institutionProductDto) {
        return institutionProductMapper.exportInsProduct(institutionProductDto);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 分页查询机构产品审核信息
     **/
    @Override
    public PageInfo<InstitutionProductVO> pageFindInsProductAudit(InstitutionProductDTO institutionProductDto) {
        return new PageInfo<InstitutionProductVO>(institutionProductAuditMapper.pageFindInsProductAudit(institutionProductDto));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据产品Id查询产品详情
     **/
    @Override
    public InstitutionProductVO getInsProductById(String insProductId, String language) {
        //获取当前请求的语言
        language = auditorProvider.getLanguage();//设置语言
        return institutionProductMapper.getInsProductById(insProductId, language);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据产品Id查询产品审核详情
     **/
    @Override
    public InstitutionProductVO getInsProductAuditById(String insProductId, String language) {
        //获取当前请求的语言
        language = auditorProvider.getLanguage();//设置语言
        return institutionProductAuditMapper.getInsProductAuditById(insProductId, language);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据机构编码查询机构所有产品
     **/
    @Override
    public List<ProChannelVO> getProChannelByInstitutionCode(String institutionCode, String language) {
        //获取当前请求的语言
        language = auditorProvider.getLanguage();//设置语言
        return productChannelMapper.getProChannelByInstitutionCode(institutionCode, language);
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 修改产品信息
     **/
    @Override
    public int updateInfoProduct(String modifier, InstitutionProductDTO institutionProductDto) {
        Date date = institutionProductDto.getProductEffectTime();
        Date date1 = DateToolUtils.getDayStart(DateToolUtils.addDay(new Date(), 1));
        if (date.getTime() < date1.getTime()) {
            throw new BusinessException(EResultEnum.EFFECTTIME_IS_ILLEGAL.getCode());
        }

        if (qrtzJobDetailsMapper.getCountByInsProId(institutionProductDto.getInsProductId().concat("_PRODUCT_INFO")) > 0) {
            throw new BusinessException(EResultEnum.AUDIT_INFO_EXIENT.getCode());
        }
        int num = 0;
        InstitutionProductAudit oldInstitutionProduct = institutionProductAuditMapper.selectByPrimaryKey(institutionProductDto.getInsProductId());
        InstitutionProduct oldInsPro = institutionProductMapper.selectByPrimaryKey(institutionProductDto.getInsProductId());
        InstitutionProductAudit institutionProduct = new InstitutionProductAudit();
        //如果该机构已经不存在或者禁用的话，是不允许进行修改的
        Institution institution = institutionMapper.selectByPrimaryKey(oldInsPro.getInstitutionId());
        if (institution == null) {//机构信息不存在
            throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());//机构信息不存在
        }
        //机构已禁用
        if (!institution.getEnabled()) {
            throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());//机构已禁用
        }
        if (!StringUtils.isEmpty(institution.getAgencyCode())) {
            //代理商机构信息
            Institution agencyInstitution = institutionMapper.getInstitutionByCode(institution.getAgencyCode());
            InstitutionProduct agencyInstitutionProduct = institutionProductMapper.getInstitutionProductByInstitutionIdAndProductId(agencyInstitution.getId(),institutionProductDto.getProductId());
            if (agencyInstitutionProduct != null && agencyInstitutionProduct.getRateType().equals(institutionProductDto.getRateType())) {
                //单笔定额的场合
                if (TradeConstant.FEE_TYPE_QUOTA.equals(institutionProductDto.getRateType())) {
                    if (institutionProductDto.getRate().compareTo(agencyInstitutionProduct.getRate()) == -1) {
                        throw new BusinessException(EResultEnum.RATE_IS_ILLEGAL.getCode());
                    }
                } else if (TradeConstant.FEE_TYPE_RATE.equals(institutionProductDto.getRateType())) {
                    //单笔费率
                    if (institutionProductDto.getRate().compareTo(agencyInstitutionProduct.getRate()) == -1 ||
                            institutionProductDto.getMinRate().compareTo(agencyInstitutionProduct.getMinTate()) == -1 ||
                            institutionProductDto.getMaxRate().compareTo(agencyInstitutionProduct.getMaxTate()) == -1) {
                        throw new BusinessException(EResultEnum.RATE_IS_ILLEGAL.getCode());
                    }
                }
            }
        }
        if (oldInstitutionProduct == null) {
            BeanUtils.copyProperties(oldInsPro, institutionProduct);
            institutionProduct.setId(institutionProductDto.getInsProductId());
            institutionProduct.setFloatRate(institutionProductDto.getFloatRate());
            institutionProduct.setEnabled(institutionProductDto.getEnabled());
            institutionProduct.setRateType(institutionProductDto.getRateType());
            institutionProduct.setRate(institutionProductDto.getRate());
            //费率类型为单笔费率的场合才有费率最小值和费率最大值
            if (institutionProductDto.getRateType() != null && TradeConstant.FEE_TYPE_RATE.equals(institutionProductDto.getRateType())) {
                institutionProduct.setMinTate(institutionProductDto.getMinRate());//费率最小值
                institutionProduct.setMaxTate(institutionProductDto.getMaxRate());//费率最大值
            }
            //退款费率相关字段
            if (institutionProductDto.getRefundRateType() != null && TradeConstant.FEE_TYPE_RATE.equals(institutionProductDto.getRefundRateType())) {
                institutionProduct.setRefundMaxTate(institutionProductDto.getRefundMaxTate());//退款费率最大值
                institutionProduct.setRefundMinTate(institutionProductDto.getRefundMinTate());//退款费率最小值
            }
            institutionProduct.setRefundAddValue(institutionProductDto.getRefundAddValue());//退款附加值
            institutionProduct.setRefundRate(institutionProductDto.getRefundRate());//退款费率
            institutionProduct.setRefundRateType(institutionProductDto.getRefundRateType());//退款费率类型

            institutionProduct.setSettleCycle(institutionProductDto.getSettleCycle());
            institutionProduct.setFeePayer(institutionProductDto.getFeePayer());
            institutionProduct.setAuditInfoStatus(TradeConstant.AUDIT_WAIT);
            institutionProduct.setAddValue(institutionProductDto.getAddValue());
            institutionProduct.setDividedMode(institutionProductDto.getDividedMode());//分润模式
            institutionProduct.setDividedRatio(institutionProductDto.getDividedRatio());//分润比例
            institutionProduct.setInfoEffectTime(institutionProductDto.getProductEffectTime());
            institutionProduct.setCreateTime(new Date());
            institutionProduct.setCreator(oldInsPro.getCreator());
            institutionProduct.setModifier(modifier);
            institutionProduct.setUpdateTime(oldInsPro.getCreateTime());
            num = institutionProductAuditMapper.insert(institutionProduct);
        } else if (oldInstitutionProduct.getAuditInfoStatus() == TradeConstant.AUDIT_FAIL || oldInstitutionProduct.getAuditInfoStatus() == TradeConstant.AUDIT_SUCCESS) {
            institutionProduct.setId(institutionProductDto.getInsProductId());
            institutionProduct.setEnabled(institutionProductDto.getEnabled());
            institutionProduct.setFloatRate(institutionProductDto.getFloatRate());
            institutionProduct.setInfoEffectTime(institutionProductDto.getProductEffectTime());
            institutionProduct.setRateType(institutionProductDto.getRateType());
            institutionProduct.setRate(institutionProductDto.getRate());
            //费率类型为单笔费率的场合才有费率最小值和费率最大值
            if (institutionProductDto.getRateType() != null && TradeConstant.FEE_TYPE_RATE.equals(institutionProductDto.getRateType())) {
                institutionProduct.setMinTate(institutionProductDto.getMinRate());//费率最小值
                institutionProduct.setMaxTate(institutionProductDto.getMaxRate());//费率最大值
            }

            //退款费率相关字段
            if (institutionProductDto.getRefundRateType() != null && TradeConstant.FEE_TYPE_RATE.equals(institutionProductDto.getRefundRateType())) {
                institutionProduct.setRefundMaxTate(institutionProductDto.getRefundMaxTate());//退款费率最大值
                institutionProduct.setRefundMinTate(institutionProductDto.getRefundMinTate());//退款费率最小值
            }
            institutionProduct.setRefundAddValue(institutionProductDto.getRefundAddValue());//退款附加值
            institutionProduct.setRefundRate(institutionProductDto.getRefundRate());//退款费率
            institutionProduct.setRefundRateType(institutionProductDto.getRefundRateType());//退款费率类型
            institutionProduct.setDividedMode(institutionProductDto.getDividedMode());//分润模式
            institutionProduct.setDividedRatio(institutionProductDto.getDividedRatio());//分润比例
            institutionProduct.setAddValue(institutionProductDto.getAddValue());
            institutionProduct.setFeePayer(institutionProductDto.getFeePayer());
            institutionProduct.setSettleCycle(institutionProductDto.getSettleCycle());
            institutionProduct.setAuditInfoStatus(TradeConstant.AUDIT_WAIT);
            institutionProduct.setModifier(modifier);
            institutionProduct.setCreateTime(new Date());
            num = institutionProductAuditMapper.updateByPrimaryKeySelective(institutionProduct);
        } else if (oldInstitutionProduct.getAuditInfoStatus() == TradeConstant.AUDIT_WAIT || oldInstitutionProduct.getAuditInfoStatus() == null) {
            throw new BusinessException(EResultEnum.AUDIT_INFO_EXIENT.getCode());
        }
        return num;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 批量审核产品信息
     **/
    @Override
    public BaseResponse auditInfoProduct(String modifier, AuaditProductDTO auaditProductDTO) {
        BaseResponse baseResponse = new BaseResponse();
        List<String> list = auaditProductDTO.getInsProductId();
        int num = 0;
        for (String insProductId : list) {
            InstitutionProduct oldInstitutionProduct = institutionProductMapper.selectByPrimaryKey(insProductId);
            //如果该机构已经不存在或者禁用的话，是不允许进行审核的
            Institution institution = institutionMapper.selectByPrimaryKey(oldInstitutionProduct.getInstitutionId());
            if (institution == null) {//机构信息不存在
                throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());//机构信息不存在
            }
            //机构已禁用
            if (!institution.getEnabled()) {
                throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());//机构已禁用
            }
            InstitutionProductAudit oldInstitutionProductAudit = institutionProductAuditMapper.selectByPrimaryKey(insProductId);
            if (oldInstitutionProduct.getAuditInfoStatus() == TradeConstant.AUDIT_SUCCESS) {
                if (auaditProductDTO.enabled) {
                    //非初次添加信息审核通过
                    //构建job信息
                    String name = insProductId;
                    String group = insProductId.concat("_PRODUCT_INFO");
                    JobDataMap jobDataMap = new JobDataMap();
                    jobDataMap.put("insProductId", insProductId);
                    JobDetail jobDetail = JobBuilder.newJob(ProductInfoJob.class).withIdentity(name, group).setJobData(jobDataMap).build();
                    //表达式调度构建器(即任务执行的时间)
                    Date runDate = oldInstitutionProductAudit.getInfoEffectTime();
                    if (runDate == null) {
                        baseResponse.setCode(EResultEnum.EFFECTTIME_IS_NULL.getCode());//生效时间不能为空
                        return baseResponse;
                    }
                    if (runDate.compareTo(new Date()) < 0) {
                        InstitutionProductHistory institutionProductAuditHistory = new InstitutionProductHistory();
                        BeanUtils.copyProperties(oldInstitutionProductAudit, institutionProductAuditHistory);
                        institutionProductAuditHistory.setId(IDS.uuid2());
                        institutionProductAuditHistory.setInsProId(oldInstitutionProductAudit.getId());
                        institutionProductAuditHistory.setAuditInfoStatus(TradeConstant.AUDIT_FAIL);
                        institutionProductAuditHistory.setAuditInfoRemark("生效时间不合法");
                        institutionProductHistoryMapper.insert(institutionProductAuditHistory);
                        //原记录信息审核状态记录失败
                        oldInstitutionProductAudit.setAuditInfoStatus(TradeConstant.AUDIT_FAIL);
                        //oldInstitutionProductAudit.setUpdateTime(new Date());
                        oldInstitutionProductAudit.setModifier(modifier);
                        oldInstitutionProductAudit.setAuditInfoRemark(auaditProductDTO.getRemarks());
                        institutionProductAuditMapper.updateByPrimaryKeySelective(oldInstitutionProductAudit);
                        baseResponse.setCode(EResultEnum.EFFECTTIME_IS_ILLEGAL.getCode());//生效时间不合法
                        return baseResponse;
                    }
                    //更改审核信息状态
                    oldInstitutionProductAudit.setAuditInfoStatus(TradeConstant.AUDIT_SUCCESS);
                    institutionProductAuditMapper.updateByPrimaryKeySelective(oldInstitutionProductAudit);
                    //根据配置动态生成cron表达式
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(runDate);
                    String yyyy = String.valueOf(calendar.get(Calendar.YEAR));
                    String mm = String.valueOf(calendar.get(Calendar.MONTH) + 1);
                    String dd = String.valueOf(calendar.get(Calendar.DATE));
                    String HH = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                    String minute = String.valueOf(calendar.get(Calendar.MINUTE));
                    String ss = String.valueOf(calendar.get(Calendar.SECOND));
                    //生成 eg:【30 45 10 20 8 2018】格式 固定时间执行任务
                    String cronExpression = ss.concat(" ").concat(minute)
                            .concat(" ").concat(HH)
                            .concat(" ").concat(dd)
                            .concat(" ").concat(mm)
                            .concat(" ").concat("?")
                            .concat(" ").concat(yyyy);
                    CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
                    //按新的cronExpression表达式构建一个新的trigger
                    CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group)
                            .withSchedule(scheduleBuilder).build();
                    try {
                        scheduler.scheduleJob(jobDetail, trigger);
                    } catch (SchedulerException e) {
                        e.printStackTrace();
                    }

                } else {
                    //非初次添加信息审核不通过
                    oldInstitutionProductAudit.setAuditInfoStatus(TradeConstant.AUDIT_FAIL);
                    //oldInstitutionProductAudit.setUpdateTime(new Date());
                    oldInstitutionProductAudit.setModifier(modifier);
                    oldInstitutionProductAudit.setAuditInfoRemark(auaditProductDTO.getRemarks());
                    institutionProductAuditMapper.updateByPrimaryKeySelective(oldInstitutionProductAudit);

                    InstitutionProductHistory institutionProductAuditHistory = new InstitutionProductHistory();
                    BeanUtils.copyProperties(oldInstitutionProductAudit, institutionProductAuditHistory);
                    institutionProductAuditHistory.setId(IDS.uuid2());
                    institutionProductAuditHistory.setInsProId(oldInstitutionProductAudit.getId());
                    institutionProductAuditHistory.setAuditInfoStatus(TradeConstant.AUDIT_FAIL);
                    institutionProductHistoryMapper.insert(institutionProductAuditHistory);

                }
            } else {
                //初次添加审核通过
                if (auaditProductDTO.enabled) {
                    //初次添加信息审核通过 更改主表，审核表审核信息状态
                    InstitutionProduct institutionProduct = institutionProductMapper.selectByPrimaryKey(insProductId);
                    institutionProduct.setId(insProductId);
                    institutionProduct.setAuditInfoStatus(TradeConstant.AUDIT_SUCCESS);
                    institutionProduct.setModifier(modifier);
                    institutionProduct.setUpdateTime(new Date());

                    InstitutionProductAudit institutionProductAudit = new InstitutionProductAudit();
                    institutionProductAudit.setId(insProductId);
                    institutionProductAudit.setAuditInfoStatus(TradeConstant.AUDIT_SUCCESS);
                    institutionProductAudit.setModifier(modifier);
                    //institutionProductAudit.setUpdateTime(new Date());
                    institutionProduct.setEnabled(true);

                    institutionProduct.setCreateTime(institutionProductAudit.getUpdateTime());

                    institutionProductMapper.updateByPrimaryKeySelective(institutionProduct);
                    institutionProductAuditMapper.updateByPrimaryKeySelective(institutionProductAudit);

                    //添加账户
                    Account account = new Account();
                    //账户关联表 自动结算开关和最小提现金额
                    SettleControl settleControl = new SettleControl();
                    String currency = productMapper.selectByPrimaryKey(institutionProductMapper.selectByPrimaryKey(insProductId).getProductId()).getCurrency();
                    if (accountMapper.getCountByinstitutionIdAndCurry(institutionMapper.selectByPrimaryKey(oldInstitutionProduct.getInstitutionId()).getInstitutionCode(), currency) == 0) {
                        account.setAccountCode(IDS.uniqueID().toString());
                        String institutionCode = institutionMapper.selectByPrimaryKey(oldInstitutionProduct.getInstitutionId()).getInstitutionCode();
                        account.setInstitutionId(institutionCode);//机构code
                        account.setInstitutionName(institutionMapper.selectByPrimaryKey(oldInstitutionProduct.getInstitutionId()).getCnName());//机构名称
                        account.setCurrency(currency);//币种
                        account.setId(IDS.uuid2());
                        account.setSettleBalance(BigDecimal.ZERO);//默认结算金额为0
                        account.setClearBalance(BigDecimal.ZERO);//默认清算金额为0
                        account.setFreezeBalance(BigDecimal.ZERO);//默认冻结金额为0
                        account.setEnabled(true);//产品审核通过以后默认币种的状态是启用的
                        account.setCreateTime(new Date());//创建时间
                        account.setCreator(modifier);//创建人
                        account.setRemark("产品信息审核通过后自动创建币种的账户");
                        //账户关联表id
                        settleControl.setId(IDS.uuid2());
                        //账户id
                        settleControl.setAccountId(account.getId());
                        //设置最小提现金额为0
                        settleControl.setMinSettleAmount(BigDecimal.ZERO);
                        settleControl.setCreateTime(new Date());
                        settleControl.setEnabled(true);
                        settleControl.setCreator(modifier);
                        settleControl.setRemark("产品信息审核通过后自动创建币种的结算控制信息");
                        if (accountMapper.insertSelective(account) > 0 && settleControlMapper.insertSelective(settleControl) > 0) {
                            redisService.set(AsianWalletConstant.ACCOUNT_CACHE_KEY.concat("_").concat(institutionCode).concat("_").concat(currency), JSON.toJSONString(account));
                        }
                    }
                    //若审核表限额状态为成功,删除审核记录
                    if (oldInstitutionProductAudit.getAuditLimitStatus() == TradeConstant.AUDIT_SUCCESS) {
                        institutionProductAuditMapper.deleteByPrimaryKey(insProductId);
                    }
                    //审核通过后将新增和修改的机构产品信息添加的redis里
                    try {
                        redisService.set(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(institutionProduct.getInstitutionId().concat("_").concat(institutionProduct.getProductId())), JSON.toJSONString(institutionProduct));
                    } catch (Exception e) {
                        log.error("审核通过后将新增和修改的机构产品信息添加的redis里：" + e.getMessage());
                        throw new BusinessException(EResultEnum.ERROR_REDIS_UPDATE.getCode());
                    }
                } else {
                    //初次添加审核不通过
                    InstitutionProduct institutionProduct = new InstitutionProduct();
                    institutionProduct.setId(insProductId);
                    institutionProduct.setAuditInfoStatus(TradeConstant.AUDIT_FAIL);
                    institutionProduct.setAuditInfoRemark(auaditProductDTO.getRemarks());
                    institutionProduct.setModifier(modifier);
                    institutionProduct.setUpdateTime(new Date());
                    num = institutionProductMapper.updateByPrimaryKeySelective(institutionProduct);

                    InstitutionProductAudit institutionProductAudit = new InstitutionProductAudit();
                    institutionProductAudit.setId(insProductId);
                    institutionProductAudit.setAuditInfoStatus(TradeConstant.AUDIT_FAIL);
                    institutionProductAudit.setAuditInfoRemark(auaditProductDTO.getRemarks());
                    institutionProductAudit.setModifier(modifier);
                    //institutionProductAudit.setUpdateTime(new Date());
                    institutionProductAuditMapper.updateByPrimaryKeySelective(institutionProductAudit);

                    InstitutionProductHistory institutionProductAuditHistory = new InstitutionProductHistory();
                    BeanUtils.copyProperties(oldInstitutionProductAudit, institutionProductAuditHistory);
                    institutionProductAuditHistory.setId(IDS.uuid2());
                    institutionProductAuditHistory.setInsProId(oldInstitutionProductAudit.getId());
                    institutionProductAuditHistory.setAuditInfoStatus(TradeConstant.AUDIT_FAIL);
                    institutionProductHistoryMapper.insert(institutionProductAuditHistory);

                }
            }


        }
        return ResultUtil.success(num);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 修改产品限额
     **/
    @Override
    public int updateLimitProduct(String modifier, InstitutionProductDTO institutionProductDto) {
        int num = 0;
        InstitutionProductAudit oldInstitutionProduct = institutionProductAuditMapper.selectByPrimaryKey(institutionProductDto.getInsProductId());
        InstitutionProduct oldInsPro = institutionProductMapper.selectByPrimaryKey(institutionProductDto.getInsProductId());
        InstitutionProductAudit institutionProduct = new InstitutionProductAudit();
        //如果该机构已经不存在或者禁用的话，是不允许进行修改的
        Institution institution = institutionMapper.selectByPrimaryKey(oldInsPro.getInstitutionId());
        if (institution == null) {//机构信息不存在
            throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());//机构信息不存在
        }
        //机构已禁用
        if (!institution.getEnabled()) {
            throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());//机构已禁用
        }
        if (oldInstitutionProduct == null) {
            BeanUtils.copyProperties(oldInsPro, institutionProduct);
            institutionProduct.setId(institutionProductDto.getInsProductId());
            institutionProduct.setAuditLimitStatus(TradeConstant.AUDIT_WAIT);
            institutionProduct.setLimitAmount(institutionProductDto.getLimitAmount());
            institutionProduct.setDailyTotalAmount(institutionProductDto.getDailyTotalAmount());
            institutionProduct.setDailyTradingCount(institutionProductDto.getDailyTradingCount());
            institutionProduct.setLimitEffectTime(institutionProductDto.getProductEffectTime());
            institutionProduct.setCreateTime(new Date());
            institutionProduct.setCreator(modifier);
            institutionProduct.setModifier(modifier);
            institutionProduct.setUpdateTime(oldInsPro.getCreateTime());
            num = institutionProductAuditMapper.insert(institutionProduct);
        } else if (oldInstitutionProduct.getAuditLimitStatus() == TradeConstant.AUDIT_FAIL || oldInstitutionProduct.getAuditLimitStatus() == TradeConstant.AUDIT_SUCCESS) {
            institutionProduct.setId(institutionProductDto.getInsProductId());
            institutionProduct.setLimitAmount(institutionProductDto.getLimitAmount());
            institutionProduct.setDailyTotalAmount(institutionProductDto.getDailyTotalAmount());
            institutionProduct.setDailyTradingCount(institutionProductDto.getDailyTradingCount());
            institutionProduct.setAuditLimitStatus(TradeConstant.AUDIT_WAIT);
            institutionProduct.setLimitEffectTime(institutionProductDto.getProductEffectTime());
            institutionProduct.setModifier(modifier);
            institutionProduct.setCreateTime(new Date());
            num = institutionProductAuditMapper.updateByPrimaryKeySelective(institutionProduct);
        } else if (oldInstitutionProduct.getAuditLimitStatus() == TradeConstant.AUDIT_WAIT || oldInstitutionProduct.getAuditLimitStatus() == null) {
            throw new BusinessException(EResultEnum.AUDIT_INFO_EXIENT.getCode());
        }
        return num;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 启用禁用机构产品
     **/
    @Override
    public int updateProductEnable(String modifier, InstitutionProductDTO institutionProductDto) {
        InstitutionProduct institutionProduct = new InstitutionProduct();
        institutionProduct.setId(institutionProductDto.getInsProductId());
        institutionProduct.setEnabled(institutionProductDto.getEnabled());
        institutionProduct.setModifier(modifier);
        institutionProduct.setUpdateTime(new Date());
        return institutionProductMapper.updateByPrimaryKeySelective(institutionProduct);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 批量审核产品限额
     **/
    @Override
    public int auditLimitProduct(String modifier, AuaditProductDTO auaditProductDTO) {
        List<String> list = auaditProductDTO.getInsProductId();
        int num = 0;
        for (String insProductId : list) {
            InstitutionProduct oldInstitutionProduct = institutionProductMapper.selectByPrimaryKey(insProductId);
            //如果该机构已经不存在或者禁用的话，是不允许进行审核的
            Institution institution = institutionMapper.selectByPrimaryKey(oldInstitutionProduct.getInstitutionId());
            if (institution == null) {//机构信息不存在
                throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());//机构信息不存在
            }
            //机构已禁用
            if (!institution.getEnabled()) {
                throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());//机构已禁用
            }
            InstitutionProductAudit oldInstitutionProductAudit = institutionProductAuditMapper.selectByPrimaryKey(insProductId);
            if (oldInstitutionProduct.getAuditLimitStatus() == TradeConstant.AUDIT_SUCCESS) {
                if (auaditProductDTO.enabled) {
                    //更改审核限额状态
                    oldInstitutionProductAudit.setAuditLimitStatus(TradeConstant.AUDIT_SUCCESS);
                    institutionProductAuditMapper.updateByPrimaryKeySelective(oldInstitutionProductAudit);

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
                    institutionProduct.setAuditLimitStatus(TradeConstant.AUDIT_SUCCESS);
                    institutionProduct.setLimitAmount(oldInstitutionProductAudit.getLimitAmount());
                    institutionProduct.setDailyTotalAmount(oldInstitutionProductAudit.getDailyTotalAmount());
                    institutionProduct.setDailyTradingCount(oldInstitutionProductAudit.getDailyTradingCount());
                    institutionProduct.setModifier(oldInstitutionProductAudit.getModifier());
                    institutionProduct.setCreateTime(oldInstitutionProduct.getUpdateTime());
                    institutionProduct.setCreator(oldInstitutionProduct.getCreator());
                    institutionProduct.setUpdateTime(new Date());
                    institutionProductMapper.updateByPrimaryKeySelective(institutionProduct);

                    //若审核表限额状态为成功，删除审核表记录
                    if (oldInstitutionProductAudit.getAuditInfoStatus() == TradeConstant.AUDIT_SUCCESS) {
                        institutionProductAuditMapper.deleteByPrimaryKey(insProductId);
                    }
                    try {
                        //审核通过后将新增和修改的机构产品信息添加的redis里
                        redisService.set(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(oldInstitutionProductAudit.getInstitutionId()).concat("_").concat(oldInstitutionProductAudit.getProductId()), JSON.toJSONString(institutionProductMapper.selectByPrimaryKey(insProductId)));
                    } catch (Exception e) {
                        log.error("机构产品限额审核通过后将机构产品信息同步到redis里发生错误：", e.getMessage());
                        throw new BusinessException(EResultEnum.ERROR_REDIS_UPDATE.getCode());
                    }

                } else {
                    //非初次添加限额审核不通过
                    oldInstitutionProductAudit.setAuditLimitStatus(TradeConstant.AUDIT_FAIL);
                    //oldInstitutionProductAudit.setUpdateTime(new Date());
                    oldInstitutionProductAudit.setModifier(modifier);
                    oldInstitutionProductAudit.setAuditLimitRemark(auaditProductDTO.getRemarks());
                    institutionProductAuditMapper.updateByPrimaryKeySelective(oldInstitutionProductAudit);

                    InstitutionProductHistory institutionProductAuditHistory = new InstitutionProductHistory();
                    BeanUtils.copyProperties(oldInstitutionProductAudit, institutionProductAuditHistory);
                    institutionProductAuditHistory.setId(IDS.uuid2());
                    institutionProductAuditHistory.setInsProId(oldInstitutionProductAudit.getId());
                    institutionProductAuditHistory.setAuditInfoStatus(TradeConstant.AUDIT_FAIL);
                    institutionProductHistoryMapper.insert(institutionProductAuditHistory);

                }
            } else {
                //初次添加审核通过
                if (auaditProductDTO.enabled) {
                    //初次添加限额审核通过,信息审核不通过
                    InstitutionProduct institutionProduct = new InstitutionProduct();
                    institutionProduct.setId(insProductId);
                    institutionProduct.setAuditLimitStatus(TradeConstant.AUDIT_SUCCESS);
                    institutionProduct.setModifier(modifier);
                    institutionProduct.setUpdateTime(new Date());

                    InstitutionProductAudit institutionProductAudit = new InstitutionProductAudit();
                    institutionProductAudit.setId(insProductId);
                    institutionProductAudit.setAuditLimitStatus(TradeConstant.AUDIT_SUCCESS);
                    institutionProductAudit.setModifier(modifier);
                    //institutionProductAudit.setUpdateTime(new Date());

                    institutionProductMapper.updateByPrimaryKeySelective(institutionProduct);
                    institutionProductAuditMapper.updateByPrimaryKeySelective(institutionProductAudit);
                    //若审核表限额状态为成功,删除审核记录
                    if (oldInstitutionProductAudit.getAuditInfoStatus() == TradeConstant.AUDIT_SUCCESS) {
                        institutionProductAuditMapper.deleteByPrimaryKey(insProductId);
                    }
                    try {
                        //审核通过后将新增和修改的机构产品信息添加的redis里
                        redisService.set(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(oldInstitutionProductAudit.getInstitutionId()).concat("_").concat(oldInstitutionProductAudit.getProductId()), JSON.toJSONString(institutionProductMapper.selectByPrimaryKey(insProductId)));
                    } catch (Exception e) {
                        log.error("机构产品限额审核通过后将机构产品信息同步到redis里发生错误：" + e.getMessage());
                        throw new BusinessException(EResultEnum.ERROR_REDIS_UPDATE.getCode());
                    }
                } else {
                    //初次添加审核不通过
                    InstitutionProduct institutionProduct = new InstitutionProduct();
                    institutionProduct.setId(insProductId);
                    institutionProduct.setAuditLimitStatus(TradeConstant.AUDIT_FAIL);
                    institutionProduct.setAuditLimitRemark(auaditProductDTO.getRemarks());
                    institutionProduct.setModifier(modifier);
                    institutionProduct.setUpdateTime(new Date());
                    num = institutionProductMapper.updateByPrimaryKeySelective(institutionProduct);

                    InstitutionProductAudit institutionProductAudit = new InstitutionProductAudit();
                    institutionProductAudit.setId(insProductId);
                    institutionProductAudit.setAuditLimitStatus(TradeConstant.AUDIT_FAIL);
                    institutionProductAudit.setAuditLimitRemark(auaditProductDTO.getRemarks());
                    institutionProductAudit.setModifier(modifier);
                    //institutionProductAudit.setUpdateTime(new Date());
                    institutionProductAuditMapper.updateByPrimaryKeySelective(institutionProductAudit);

                    InstitutionProductHistory institutionProductAuditHistory = new InstitutionProductHistory();
                    BeanUtils.copyProperties(oldInstitutionProductAudit, institutionProductAuditHistory);
                    institutionProductAuditHistory.setId(IDS.uuid2());
                    institutionProductAuditHistory.setInsProId(oldInstitutionProductAudit.getId());
                    institutionProductAuditHistory.setAuditInfoStatus(TradeConstant.AUDIT_FAIL);
                    institutionProductHistoryMapper.insert(institutionProductAuditHistory);


                }
            }
        }
        return num;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 机构产品分配通道
     **/
    @Override
    public int allotProductChannel(String modifier, InstProdDTO instProdDTO) {
        log.info("----------------- 机构产品分配通道 ---------------- modifier : {},instProdDTO : {} ", modifier, JSONObject.toJSONString(instProdDTO));
        //必填的check
        if (instProdDTO == null) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //必填的check
        if (StringUtils.isEmpty(instProdDTO.getInstitutionId()) || StringUtils.isEmpty(instProdDTO.getProductList()) || instProdDTO.getProductList().size() == 0) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        List<InstitutionChannel> list = Lists.newArrayList();
        for (ProdChannelDTO prodChannelDTO : instProdDTO.getProductList()) {
            //根据机构id与产品id查询机构产品中间表id
            String insProId = institutionProductMapper.selectByInstitutionIdAndProductId(instProdDTO.getInstitutionId(), prodChannelDTO.getProductId());
            List<InstitutionChannel> list1 = institutionChannelMapper.selectByInsProId(insProId);
            //根据机构产品中间表id删除机构通道信息
            institutionChannelMapper.deleteByInsProId(insProId);
            for (ChannelInfoDTO channelInfoDTO : prodChannelDTO.getChannelList()) {
                for (BankInfoDTO bankInfoDTO : channelInfoDTO.getBankList()) {
                    ChaBankRelVO chaBankRelVO = channelBankMapper.getInfoByCIdAndBId(channelInfoDTO.getChannelId(), bankInfoDTO.getBankId());
                    InstitutionChannel institutionChannel = new InstitutionChannel();
                    institutionChannel.setId(IDS.uuid2());
                    institutionChannel.setInsProId(insProId);
                    //通道银行id
                    institutionChannel.setChannelId(chaBankRelVO.getChabankId());
                    institutionChannel.setCreateTime(new Date());
                    institutionChannel.setCreator(modifier);
                    institutionChannel.setSort(chaBankRelVO.getSort());
                    institutionChannel.setEnabled(true);
                    boolean flag = true;
                    for (InstitutionChannel ic : list1) {
                        if (institutionChannel.getInsProId().equals(ic.getInsProId()) && institutionChannel.getChannelId().equals(ic.getChannelId())) {
                            flag = false;
                            list.add(ic);
                        }
                    }
                    if (flag) {
                        list.add(institutionChannel);
                    }
                }
            }
        }
        institutionChannelMapper.insertList(list);
        for (InstitutionChannel institutionChannel : list) {
            //审核通过后将新增和修改的通道信息添加的redis里
            List<String> channelIds = institutionChannelMapper.selectChannelCodeByInsProId(institutionChannel.getInsProId());
            try {
                redisService.set(AsianWalletConstant.INSTITUTIONCHANNEL_CACHE_KEY.concat("_").concat(institutionChannel.getInsProId()),
                        JSON.toJSONString(channelIds));
            } catch (Exception e) {
                log.error("审核通过后将新增和修改的通道信息添加的redis里：", e.getMessage());
                throw new BusinessException(EResultEnum.ERROR_REDIS_UPDATE.getCode());
            }
        }
        return 0;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 分页查询产品通道管理信息
     **/
    @Override
    public PageInfo<ProductChannelVO> pageFindProductChannel(SearchChannelDTO searchChannelDTO) {
        return new PageInfo<ProductChannelVO>(institutionChannelMapper.pageFindProductChannel(searchChannelDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 删除产品通道管理信息
     **/
    @Override
    public int deleteProductChannel(String insChaId) {
        return institutionChannelMapper.deleteByPrimaryKey(insChaId);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 启用禁用产品通道
     **/
    @Override
    public int banProductChannel(String insChaId, Boolean enabled) {
        InstitutionChannel institutionChannel = institutionChannelMapper.selectByPrimaryKey(insChaId);
        institutionChannel.setId(insChaId);
        institutionChannel.setEnabled(enabled);
        institutionChannel.setUpdateTime(new Date());
        int num = institutionChannelMapper.updateByPrimaryKeySelective(institutionChannel);
        //审核通过后将新增和修改的通道信息添加的redis里
        List<String> list = institutionChannelMapper.selectChannelCodeByInsProId(institutionChannel.getInsProId());
        try {
            redisService.set(AsianWalletConstant.INSTITUTIONCHANNEL_CACHE_KEY.concat("_").concat(institutionChannel.getInsProId()),
                    JSON.toJSONString(list));
        } catch (Exception e) {
            log.error("审核通过后将新增和修改的通道信息添加的redis里：", e.getMessage());
            throw new BusinessException(EResultEnum.ERROR_REDIS_UPDATE.getCode());
        }
        return num;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/30
     * @Descripate 修改机构通道优先级
     **/
    @Override
    public int updateSort(String modifier, String insChaId, String sort, boolean enabled) {
        InstitutionChannel institutionChannel = institutionChannelMapper.selectByPrimaryKey(insChaId);
        institutionChannel.setId(insChaId);
        institutionChannel.setSort(sort);
        institutionChannel.setEnabled(enabled);
        institutionChannel.setUpdateTime(new Date());
        institutionChannel.setModifier(modifier);
        institutionChannelMapper.updateByPrimaryKeySelective(institutionChannel);
        List<String> list = institutionChannelMapper.selectChannelCodeByInsProId(institutionChannel.getInsProId());
        try {
            redisService.set(AsianWalletConstant.INSTITUTIONCHANNEL_CACHE_KEY.concat("_").concat(institutionChannel.getInsProId()),
                    JSON.toJSONString(list));
        } catch (Exception e) {
            log.error("审核通过后将新增和修改的通道信息添加的redis里：", e.getMessage());
            throw new BusinessException(EResultEnum.ERROR_REDIS_UPDATE.getCode());
        }
        return 0;
    }

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/8/27
     * @Descripate 批量修改机构通道优先级
     **/
    @Override
    public int batchUpdateSort(String modifier, List<BatchUpdateSortDTO> batchUpdateSortList) {
        int num = 0;
        for (BatchUpdateSortDTO batchUpdateSort : batchUpdateSortList) {
            if (StringUtils.isEmpty(batchUpdateSort.getEnabled()) && StringUtils.isEmpty(batchUpdateSort.getSort())) {
                continue;
            }
            //这两个参数不为空,代表这是条需要修改的数据
            InstitutionChannel institutionChannel = institutionChannelMapper.selectByPrimaryKey(batchUpdateSort.getInsChaId());
            if(!StringUtils.isEmpty(batchUpdateSort.getSort())){
                institutionChannel.setSort(batchUpdateSort.getSort());
            }
            if(!StringUtils.isEmpty(batchUpdateSort.getEnabled())){
                institutionChannel.setEnabled(batchUpdateSort.getEnabled());
            }
            institutionChannel.setUpdateTime(new Date());
            institutionChannel.setModifier(modifier);
            num += institutionChannelMapper.updateByPrimaryKeySelective(institutionChannel);
            List<String> list = institutionChannelMapper.selectChannelCodeByInsProId(institutionChannel.getInsProId());
            //同步Redis
            redisService.set(AsianWalletConstant.INSTITUTIONCHANNEL_CACHE_KEY.concat("_").concat(institutionChannel.getInsProId()), JSON.toJSONString(list));
        }
        return num;
    }

    /**
     * 导出机构通道
     *
     * @param searchChannelExportDTO
     * @return
     */
    @Override
    public List<ProductChannelVO> exportProductChannel(SearchChannelExportDTO searchChannelExportDTO) {
        return institutionChannelMapper.exportProductChannel(searchChannelExportDTO);
    }
}
