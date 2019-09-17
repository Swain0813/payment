package com.payment.institution.service.impl;

import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseServiceImpl;
import com.payment.common.config.AuditorProvider;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.ExportAgencyInstitutionDTO;
import com.payment.common.dto.InstitutionDTO;
import com.payment.common.dto.InstitutionExportDTO;
import com.payment.common.dto.QueryAgencyInstitutionDTO;
import com.payment.common.entity.Attestation;
import com.payment.common.entity.Institution;
import com.payment.common.entity.SysUser;
import com.payment.common.entity.SysUserRole;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.IDS;
import com.payment.common.utils.RSAUtils;
import com.payment.common.vo.*;
import com.payment.institution.dao.*;
import com.payment.institution.entity.InstitutionAudit;
import com.payment.institution.entity.InstitutionHistory;
import com.payment.institution.service.InstitutionService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-08 10:19
 **/
@Slf4j
@Service
@Transactional
public class InstitutionServiceImpl extends BaseServiceImpl<Institution> implements InstitutionService {

    @Autowired
    private InstitutionMapper institutionMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private InstitutionAuditMapper institutionAuditMapper;

    @Autowired
    private InstitutionHistoryMapper institutionHistoryMapper;

    @Autowired
    private AttestationMapper attestationMapper;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AuditorProvider auditorProvider;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 添加机构信息
     **/
    @Override
    public String addInstitution(SysUserVO sysUserVO, InstitutionDTO institutionDTO) {
        //必填参数的check---机构类型
        if (StringUtils.isEmpty(institutionDTO.getInstitutionType())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //机构编号
        String str = IDS.uniqueID().toString();
        String institutionCode = DateToolUtils.getReqDateE().concat(str.substring(str.length() - 4));
        //判断登录账号是否存在
        if (sysUserMapper.getCountByUserName(institutionCode + "admin") > 0) {
            throw new BusinessException(EResultEnum.USER_EXIST.getCode());
        }
        //判断机构名称是否存在
        if (institutionMapper.selectCountByInsName(institutionDTO.getCnName()) > 0) {
            throw new BusinessException(EResultEnum.INSTITUTION_NAME_EXIST.getCode());
        }
        if (institutionMapper.selectCountByInsName(institutionDTO.getEnName()) > 0) {
            throw new BusinessException(EResultEnum.INSTITUTION_NAME_EXIST.getCode());
        }
        //机构主体信息
        InstitutionAudit institutionAudit = new InstitutionAudit();
        String institutionid = IDS.uuid2();
        BeanUtils.copyProperties(institutionDTO, institutionAudit);
        institutionAudit.setId(institutionid);
        //创建人
        institutionAudit.setCreator(sysUserVO.getUsername());
        //创建时间
        institutionAudit.setCreateTime(new Date());
        institutionAudit.setInstitutionCode(institutionCode);
        institutionAudit.setAuditStatus(TradeConstant.AUDIT_WAIT);
        institutionAudit.setEnabled(false);

        Institution institution = new Institution();
        BeanUtils.copyProperties(institutionDTO, institution);
        institution.setId(institutionid);
        //创建人
        institution.setCreator(sysUserVO.getUsername());
        //创建时间
        institution.setCreateTime(new Date());
        institution.setInstitutionCode(institutionCode);
        institution.setAuditStatus(TradeConstant.AUDIT_WAIT);
        institution.setEnabled(false);
        //机构的场合需要添加机构对应的平台公私钥
        if (institutionMapper.insert(institution) > 0 && AsianWalletConstant.TWO != institutionDTO.getInstitutionType()) {
            Attestation attestation = new Attestation();
            attestation.setId(IDS.uuid2());//id
            Map<String, String> rsaMap;
            try {
                rsaMap = RSAUtils.initKey();
            } catch (Exception e) {
                log.info("---------生成RSA公私钥错误---------");
                throw new BusinessException(EResultEnum.KEY_GENERATION_FAILED.getCode());
            }
            attestation.setInstitutionCode("PF_" + institutionCode);
            attestation.setPubkey(rsaMap.get("publicKey"));
            attestation.setPrikey(rsaMap.get("privateKey"));
            attestation.setType((byte) 1);
            attestation.setEnabled(true);
            attestation.setMd5key(IDS.uuid2());
            attestation.setCreator(sysUserVO.getUsername());
            attestation.setCreateTime(new Date());
            attestationMapper.insert(attestation);
        }
        //账号信息
        SysUser sysUser = new SysUser();
        String userId = IDS.uuid2();
        sysUser.setId(userId);
        sysUser.setUsername(institutionCode + "admin");
        sysUser.setPassword(institutionDTO.getPassword());
        sysUser.setTradePassword(encryptPassword("123456"));//交易密码
        sysUser.setInstitutionId(institutionid);
        if (AsianWalletConstant.TWO != institutionDTO.getInstitutionType()) {
            //机构系统
            sysUser.setType(AsianWalletConstant.TWO);
        } else {
            //代理商系统
            sysUser.setType(AsianWalletConstant.FOUR);
        }
        sysUser.setName("admin");
        sysUser.setLanguage(auditorProvider.getLanguage());//设置语言
        sysUser.setCreateTime(new Date());
        sysUser.setCreator(sysUserVO.getUsername());
        sysUserMapper.insert(sysUser);
        if (AsianWalletConstant.TWO != institutionDTO.getInstitutionType()) {
            //分配pos机账号
            SysUser sysUser1 = new SysUser();
            String userId1 = IDS.uuid2();
            sysUser1.setId(userId1);
            sysUser1.setUsername(institutionCode + "00");
            sysUser1.setPassword(institutionDTO.getPassword());
            sysUser1.setTradePassword(encryptPassword("123456"));//交易密码
            sysUser1.setInstitutionId(institutionid);
            sysUser1.setType(AsianWalletConstant.THREE);
            sysUser1.setName("posAdmin");
            sysUser1.setLanguage(auditorProvider.getLanguage());//设置语言
            sysUser1.setCreateTime(new Date());
            sysUser1.setCreator(sysUserVO.getUsername());
            sysUserMapper.insert(sysUser1);
            //分配机构角色
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setRoleId(sysUserRoleMapper.getInstitutionRoleId());
            sysUserRole.setUserId(userId);
            sysUserRole.setCreateTime(new Date());
            sysUserRole.setCreator(sysUserVO.getUsername());
            sysUserRoleMapper.insert(sysUserRole);
            //分配pos机角色
            SysUserRole sysUserRole1 = new SysUserRole();
            sysUserRole1.setRoleId(sysUserRoleMapper.getPOSRoleId());
            sysUserRole1.setUserId(userId1);
            sysUserRole1.setCreateTime(new Date());
            sysUserRole1.setCreator(sysUserVO.getUsername());
            sysUserRoleMapper.insert(sysUserRole1);
        } else {//分配代理商角色
            SysUserRole sysUserRole2 = new SysUserRole();
            sysUserRole2.setRoleId(sysUserRoleMapper.getAgencyRoleId());
            sysUserRole2.setUserId(userId);
            sysUserRole2.setCreateTime(new Date());
            sysUserRole2.setCreator(sysUserVO.getUsername());
            sysUserRoleMapper.insert(sysUserRole2);
        }
        institutionAuditMapper.insert(institutionAudit);
        return institutionid;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 修改机构信息
     **/
    @Override
    public int updateInstitution(SysUserVO sysUserVO, InstitutionDTO institutionDTO) {
        //机构主体信息
        //若有审核失败数据删除
        InstitutionAudit oldInstitutionAudit = institutionAuditMapper.selectByPrimaryKey(institutionDTO.getInstitutionId());
        if (oldInstitutionAudit != null && TradeConstant.AUDIT_WAIT.equals(oldInstitutionAudit.getAuditStatus())) {
            throw new BusinessException(EResultEnum.AUDIT_INFO_EXIENT.getCode());
        } else if (oldInstitutionAudit != null && TradeConstant.AUDIT_FAIL.equals(oldInstitutionAudit.getAuditStatus())) {
            institutionAuditMapper.deleteByPrimaryKey(institutionDTO.getInstitutionId());
        }
        InstitutionAudit institutionAudit = new InstitutionAudit();
        BeanUtils.copyProperties(institutionDTO, institutionAudit);
        institutionAudit.setId(institutionDTO.getInstitutionId());
        institutionAudit.setInstitutionCode(institutionDTO.getInstitutionCode());
        //创建时间
        institutionAudit.setCreateTime(new Date());
        //创建人
        institutionAudit.setCreator(sysUserVO.getUsername());
        institutionAudit.setAuditStatus(TradeConstant.AUDIT_WAIT);
        institutionAudit.setInstitutionEffectTime(institutionDTO.getInstitutionEffectTime());
        return institutionAuditMapper.insert(institutionAudit);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构信息
     **/
    @Override
    public PageInfo<Institution> pageFindInstitution(InstitutionDTO institutionDTO) {
        //获取当前请求的语言
        institutionDTO.setLanguage(auditorProvider.getLanguage());//设置语言
        return new PageInfo(institutionMapper.pageFindInstitution(institutionDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构审核信息列表
     **/
    @Override
    public PageInfo<InstitutionAudit> pageFindInstitutionAduit(InstitutionDTO institutionDTO) {
        //获取当前请求的语言
        institutionDTO.setLanguage(auditorProvider.getLanguage());//设置语言
        return new PageInfo(institutionAuditMapper.pageFindInstitutionAduit(institutionDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构历史记录信息列表
     **/
    @Override
    public PageInfo<InstitutionHistory> pageFindInstitutionHistory(InstitutionDTO institutionDTO) {
        //获取当前请求的语言
        institutionDTO.setLanguage(auditorProvider.getLanguage());//设置语言
        return new PageInfo(institutionHistoryMapper.pageFindInstitutionHistory(institutionDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构Code查询机构信息详情
     **/
    @Override
    public InstitutionAccountVO getInstitutionInfoByCode(String institutionCode) {
        //根据机构code获取机构信息
        InstitutionAccountVO institutionAccountVO = institutionMapper.getInstitutionAccountInfoByCode(institutionCode);
        if (institutionAccountVO == null) {
            throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());//机构信息不存在
        }
        //判断机构是否已经禁用
        if (!institutionAccountVO.getEnabled()) {
            throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());//机构已经禁用
        }
        return institutionAccountVO;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构id查询机构信息
     **/
    @Override
    public InstitutionDetailVO getInstitutionInfo(String id) {
        return institutionMapper.getInstitutionDetail(id);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构Id查询机构变更信息详情
     **/
    @Override
    public InstitutionDetailVO getInstitutionHistoryInfo(String id) {
        //获取当前请求的语言
        String language = auditorProvider.getLanguage();
        return institutionHistoryMapper.getInstitutionHistoryInfo(id);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构Id查询机构审核信息详情
     **/
    @Override
    public InstitutionDetailVO getInstitutionInfoAudit(String id) {
        //获取当前请求的语言
        String language = auditorProvider.getLanguage();
        return institutionAuditMapper.getInstitutionInfoAudit(id, language);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 启用禁用机构
     **/
    @Override
    public int banInstitution(String modifier, String institutionId, Boolean enabled) {
        int num;
        Institution institution = institutionMapper.selectByPrimaryKey(institutionId);
        if (institution == null) {//机构信息不存在
            throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());
        }
        institution.setId(institutionId);
        institution.setEnabled(enabled);
        institution.setModifier(modifier);
        institution.setUpdateTime(new Date());
        num = institutionMapper.updateByPrimaryKeySelective(institution);
        try {
            //更新机构信息后添加的redis里
            redisService.set(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institution.getInstitutionCode()), JSON.toJSONString(institution));
        } catch (Exception e) {
            throw new BusinessException(EResultEnum.ERROR_REDIS_UPDATE.getCode());
        }
        return num;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 审核机构信息
     **/
    @Override
    public int auditInstitution(String modifier, String institutionId, Boolean enabled, String remark) {
        int num;
        Institution oleInstitution = institutionMapper.selectByPrimaryKey(institutionId);
        if (TradeConstant.AUDIT_SUCCESS.equals(oleInstitution.getAuditStatus())) {
            if (enabled) {
                //审核通过
                //将审核表信息移动到主题表
                InstitutionAudit institutionAudit = institutionAuditMapper.selectByPrimaryKey(institutionId);
                //查询主题表原机构信息.把原机构信息存放历史表
                InstitutionHistory institutionHistory = new InstitutionHistory();
                BeanUtils.copyProperties(oleInstitution, institutionHistory);
                institutionHistory.setId(IDS.uuid2());
                institutionHistory.setInstitutionId(institutionId);
                institutionHistory.setEnabled(enabled);
                institutionHistoryMapper.insert(institutionHistory);
                institutionMapper.deleteByPrimaryKey(institutionId);
                //将审核表信息移动到主题表
                Institution institution = new Institution();
                BeanUtils.copyProperties(institutionAudit, institution);
                institution.setAuditStatus(TradeConstant.AUDIT_SUCCESS);
                //创建时间
                institution.setCreateTime(oleInstitution.getCreateTime());
                //创建人
                institution.setCreator(oleInstitution.getCreator());
                institution.setUpdateTime(new Date());
                institution.setModifier(institutionAudit.getModifier());
                institution.setRemark(remark);
                institution.setEnabled(enabled);
                institutionMapper.insert(institution);
                institutionAuditMapper.deleteByPrimaryKey(institutionId);
                try {
                    //审核通过后将新增和修改的机构信息添加的redis里
                    redisService.set(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institution.getInstitutionCode()), JSON.toJSONString(institution));
                } catch (Exception e) {
                    log.error("审核通过后将机构信息同步到redis里发生错误：", e.getMessage());
                }
                num = 0;
            } else {
                //审核不通过
                InstitutionAudit institutionAudit = new InstitutionAudit();
                institutionAudit.setAuditStatus(TradeConstant.AUDIT_FAIL);
                institutionAudit.setId(institutionId);
                institutionAudit.setModifier(modifier);
                institutionAudit.setUpdateTime(new Date());
                institutionAudit.setRemark(remark);
                institutionAudit.setEnabled(enabled);
                num = institutionAuditMapper.updateByPrimaryKeySelective(institutionAudit);
            }
        } else {
            //初次添加
            if (enabled) {
                //审核通过
                //查询主题表原机构信息.把原机构信息存放历史表
                institutionMapper.deleteByPrimaryKey(institutionId);
                //将审核表信息移动到主题表
                InstitutionAudit institutionAudit = institutionAuditMapper.selectByPrimaryKey(institutionId);
                Institution institution = new Institution();
                BeanUtils.copyProperties(institutionAudit, institution);
                institution.setAuditStatus(TradeConstant.AUDIT_SUCCESS);
                //创建时间
                institution.setCreateTime(oleInstitution.getCreateTime());
                //创建人
                institution.setCreator(oleInstitution.getCreator());
                institution.setUpdateTime(new Date());
                institution.setModifier(modifier);
                institution.setEnabled(enabled);
                institutionMapper.insert(institution);
                try {
                    //审核通过后将新增和修改的机构信息添加的redis里
                    redisService.set(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institution.getInstitutionCode()), JSON.toJSONString(institution));
                } catch (Exception e) {
                    log.error("审核通过后将机构信息同步到redis里发生错误：", e.getMessage());
                }
                num = institutionAuditMapper.deleteByPrimaryKey(institutionId);
            } else {
                Institution institution = new Institution();
                institution.setId(institutionId);
                institution.setAuditStatus(TradeConstant.AUDIT_FAIL);
                institution.setModifier(modifier);
                institution.setUpdateTime(new Date());
                institution.setRemark(remark);
                institution.setEnabled(enabled);
                institutionMapper.updateByPrimaryKeySelective(institution);
                //审核不通过
                InstitutionAudit institutionAudit = new InstitutionAudit();
                institutionAudit.setAuditStatus(TradeConstant.AUDIT_FAIL);
                institutionAudit.setId(institutionId);
                institutionAudit.setModifier(modifier);
                institutionAudit.setUpdateTime(new Date());
                institutionAudit.setRemark(remark);
                institutionAudit.setEnabled(enabled);
                num = institutionAuditMapper.updateByPrimaryKeySelective(institutionAudit);
            }

        }
        return num;
    }

    /**
     * 导出功能
     *
     * @param institutionDTO
     * @return
     */
    @Override
    public List<InstitutionExportVO> exportInformation(InstitutionExportDTO institutionDTO) {
        //获取当前请求的语言
        institutionDTO.setLanguage(auditorProvider.getLanguage());//设置语言
        List<InstitutionExportVO> institutionExportVOS = institutionMapper.selectExport(institutionDTO);
        return institutionExportVOS;
    }

    /**
     * 通过机构名称模糊查询查询机构信息
     *
     * @param institutionDTO
     * @return
     */
    @Override
    public List<Institution> getInstitutionInfoByName(InstitutionDTO institutionDTO) {
        return institutionMapper.getInstitutionInfoByName(institutionDTO);
    }


    /**
     * 根据代理机构code等查询代理机构相关信息
     *
     * @param institutionDTO
     * @return
     */
    @Override
    public List<Institution> getAgencyList(InstitutionDTO institutionDTO) {
        return institutionMapper.getAgencyList(institutionDTO);
    }

    /**
     * 代理商商户信息查询
     *
     * @param queryAgencyInstitutionDTO 代理商商户信息查询DTO
     * @return AgencyInstitutionVO
     */
    @Override
    public PageInfo<AgencyInstitutionVO> getAgencyInstitution(QueryAgencyInstitutionDTO queryAgencyInstitutionDTO) {
        queryAgencyInstitutionDTO.setLanguage(auditorProvider.getLanguage());
        queryAgencyInstitutionDTO.setSort("i.create_time");
        return new PageInfo<>(institutionMapper.pageAgencyInstitution(queryAgencyInstitutionDTO));
    }

    /**
     * 代理商商户信息导出
     *
     * @param exportAgencyInstitutionDTO 代理商商户信息导出DTO
     * @return AgencyInstitutionVO
     */
    @Override
    public List<AgencyInstitutionVO> exportAgencyInstitution(ExportAgencyInstitutionDTO exportAgencyInstitutionDTO) {
        exportAgencyInstitutionDTO.setLanguage(auditorProvider.getLanguage());
        return institutionMapper.exportAgencyInstitution(exportAgencyInstitutionDTO);
    }
}
