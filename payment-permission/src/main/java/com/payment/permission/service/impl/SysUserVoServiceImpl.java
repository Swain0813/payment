package com.payment.permission.service.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.config.AuditorProvider;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.InstitutionDTO;
import com.payment.common.entity.Attestation;
import com.payment.common.entity.SysUser;
import com.payment.common.entity.SysUserRole;
import com.payment.common.enums.Status;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.DateUtil;
import com.payment.common.utils.IDS;
import com.payment.common.utils.RSAUtils;
import com.payment.common.vo.SysMenuVO;
import com.payment.common.vo.SysRoleVO;
import com.payment.common.vo.SysUserVO;
import com.payment.permission.dao.AttestationMapper;
import com.payment.permission.dao.SysUserMapper;
import com.payment.permission.dao.SysUserMenuMapper;
import com.payment.permission.dao.SysUserRoleMapper;
import com.payment.permission.dto.SysUserRoleDto;
import com.payment.permission.entity.SysUserMenu;
import com.payment.permission.feign.message.MessageFeign;
import com.payment.permission.service.SysUserVoService;
import com.payment.permission.utils.SpringSecurityUser;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-11 10:42
 **/
@Service
@Slf4j
@Transactional
public class SysUserVoServiceImpl implements SysUserVoService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysUserMenuMapper sysUserMenuMapper;

    @Autowired
    private MessageFeign messageFeign;

    @Autowired
    private AuditorProvider auditorProvider;

    @Autowired
    private AttestationMapper attestationMapper;

    @Autowired
    private RedisService redisService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public SysUserVO getSysUser(String userName) {
        return sysUserMapper.getSysUser(userName);
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        SysUserVO user = getSysUser(userName);
        if (user == null) {
            throw new BusinessException(EResultEnum.USER_NOT_EXIST.getCode());
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        List<SysRoleVO> listrole = user.getRole();
        Set<String> set = Sets.newHashSet();
        for (SysRoleVO sysRoleVO : listrole) {
            if (sysRoleVO != null) {
                for (SysMenuVO permission : sysRoleVO.getMenus()) {
                    set.add(permission.getId());
                }
            }
        }
        for (String s : set) {
            authorities.add(new SimpleGrantedAuthority(s));
        }
        SpringSecurityUser userDetails = new SpringSecurityUser();
        userDetails.setSysUser(user);
        userDetails.setAuthorities(authorities);
        return userDetails;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/21
     * @Descripate 机构添加用户具体信息
     **/
    @Override
    public int addSysUserbyInstitution(SysUserVO sysUserVO, SysUserRoleDto sysUserRoleDto) {
        //判断机构是否存在
        String institutionCode = sysUserMapper.getInstitutionCodeByInstitutionId(sysUserRoleDto.getInstitutionId());
        if (StringUtils.isBlank(institutionCode)) {
            throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());
        }
        int num = 0;
        SysUser dbSysUser = sysUserMapper.getSysUserByUserName(institutionCode.concat(sysUserRoleDto.getUsername()));
        if (sysUserRoleDto.getFlag().equals("2")) {//修改的场合
            if (sysUserRoleDto.getTradePassword() != null) {
                dbSysUser.setTradePassword(encryptPassword(sysUserRoleDto.getTradePassword()));//交易密码
            }
            if (sysUserRoleDto.getEnabled() != null) {
                dbSysUser.setEnabled(sysUserRoleDto.getEnabled());
            }
            if (sysUserRoleDto.getName() != null) {
                dbSysUser.setName(sysUserRoleDto.getName());
            }
            if (sysUserRoleDto.getEmail() != null) {
                dbSysUser.setEmail(sysUserRoleDto.getEmail());
            }
            dbSysUser.setUpdateTime(new Date());
            dbSysUser.setModifier(sysUserVO.getName());
            num = sysUserMapper.updateByPrimaryKeySelective(dbSysUser);
        } else {//新增
            if (dbSysUser != null && dbSysUser.getType().equals(sysUserRoleDto.getType()) &&
                    dbSysUser.getUsername().equals(institutionCode.concat(sysUserRoleDto.getUsername()))) {
                throw new BusinessException(EResultEnum.USER_EXIST.getCode());
            }
            SysUser sysUser = new SysUser();
            String userId = IDS.uuid2();
            sysUserRoleDto.setUserId(userId);
            BeanUtils.copyProperties(sysUserRoleDto, sysUser);
            sysUser.setId(userId);
            sysUser.setInstitutionId(sysUserRoleDto.getInstitutionId());
            sysUser.setUsername(institutionCode + sysUserRoleDto.getUsername());
            sysUser.setEnabled(true);
            sysUser.setTradePassword(encryptPassword(sysUserRoleDto.getTradePassword()));//交易密码
            sysUser.setPassword(encryptPassword(sysUserRoleDto.getPassword()));
            sysUser.setLanguage(auditorProvider.getLanguage());//设置语言
            sysUser.setCreator(sysUserVO.getUsername());
            sysUser.setCreateTime(new Date());
            sysUserMapper.insert(sysUser);
        }

        //给用户分配角色
        sysUserRoleMapper.deleteByUserId(sysUserRoleDto.getUserId());
        if (sysUserRoleDto.getRoleId() != null && sysUserRoleDto.getRoleId().size() > 0) {
            List<SysUserRole> listRole = Lists.newArrayList();
            for (String s : sysUserRoleDto.getRoleId()) {
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setId(IDS.uuid2());
                sysUserRole.setCreateTime(new Date());
                sysUserRole.setRoleId(s);
                sysUserRole.setUserId(sysUserRoleDto.getUserId());
                sysUserRole.setCreator(sysUserRoleDto.getUsername());
                sysUserRole.setModifier(sysUserVO.getUsername());
                listRole.add(sysUserRole);
            }
            sysUserRoleMapper.insertList(listRole);
        }

        //给用户分配权限
        sysUserMenuMapper.deleteByUserId(sysUserRoleDto.getUserId());
        if (sysUserRoleDto.getMenuId() != null && sysUserRoleDto.getMenuId().size() > 0) {
            List<SysUserMenu> listMenu = Lists.newArrayList();
            for (String s : sysUserRoleDto.getMenuId()) {
                SysUserMenu sysUserMenu = new SysUserMenu();
                sysUserMenu.setId(IDS.uuid2());
                sysUserMenu.setCreateTime(new Date());
                sysUserMenu.setMenuId(s);
                sysUserMenu.setUserId(sysUserRoleDto.getUserId());
                sysUserMenu.setCreator(sysUserRoleDto.getUsername());
                sysUserMenu.setModifier(sysUserRoleDto.getUsername());
                listMenu.add(sysUserMenu);
            }
            sysUserMenuMapper.insertList(listMenu);
        }

        return num;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 运营后台添加修改用户
     **/
    @Override
    public int addSysUserbyAdmin(String creator, SysUserRoleDto sysUserRoleDto) {
        //添加用户
        sysUserRoleDto.setType(1);//运营后台用户
        int num = 0;
        SysUser dbSysUser = sysUserMapper.getSysUserByUserName(sysUserRoleDto.getUsername());
        if (sysUserRoleDto.getFlag().equals("2")) {//修改的场合
            BeanUtils.copyProperties(sysUserRoleDto, dbSysUser);
            dbSysUser.setId(sysUserRoleDto.getUserId());
            dbSysUser.setUpdateTime(new Date());
            dbSysUser.setModifier(creator);
            num = sysUserMapper.updateByPrimaryKeySelective(dbSysUser);
        } else {//新增
            if (dbSysUser != null && dbSysUser.getType().equals(sysUserRoleDto.getType()) && dbSysUser.getUsername().equals(sysUserRoleDto.getUsername())) {
                throw new BusinessException(EResultEnum.USER_EXIST.getCode());
            }
            SysUser sysUser = new SysUser();
            String userId = IDS.uuid2();
            sysUserRoleDto.setUserId(userId);
            BeanUtils.copyProperties(sysUserRoleDto, sysUser);
            sysUser.setId(sysUserRoleDto.getUserId());
            if (StringUtils.isEmpty(sysUserRoleDto.getPassword())) {
                sysUser.setPassword(encryptPassword("123456"));
            } else {
                sysUser.setPassword(encryptPassword(sysUserRoleDto.getPassword()));
            }
            if (StringUtils.isEmpty(sysUserRoleDto.getTradePassword())) {
                sysUser.setTradePassword(encryptPassword("123456"));//交易密码
            } else {
                sysUser.setTradePassword(encryptPassword(sysUserRoleDto.getTradePassword()));
            }
            sysUser.setLanguage(auditorProvider.getLanguage());//设置语言
            sysUser.setEnabled(true);
            sysUser.setCreateTime(new Date());
            sysUser.setCreator(creator);
            num = sysUserMapper.insert(sysUser);
        }

        //给用户分配角色
        sysUserRoleMapper.deleteByUserId(sysUserRoleDto.getUserId());
        if (sysUserRoleDto.getRoleId() != null && sysUserRoleDto.getRoleId().size() > 0) {
            List<SysUserRole> listRole = Lists.newArrayList();
            for (String s : sysUserRoleDto.getRoleId()) {
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setId(IDS.uuid2());
                sysUserRole.setCreateTime(new Date());
                sysUserRole.setRoleId(s);
                sysUserRole.setUserId(sysUserRoleDto.getUserId());
                sysUserRole.setModifier(creator);
                sysUserRole.setCreator(creator);
                listRole.add(sysUserRole);
            }
            sysUserRoleMapper.insertList(listRole);
        }

        //给用户分配权限
        sysUserMenuMapper.deleteByUserId(sysUserRoleDto.getUserId());
        if (sysUserRoleDto.getMenuId() != null && sysUserRoleDto.getMenuId().size() > 0) {
            List<SysUserMenu> listMenu = Lists.newArrayList();
            for (String s : sysUserRoleDto.getMenuId()) {
                SysUserMenu sysUserMenu = new SysUserMenu();
                sysUserMenu.setId(IDS.uuid2());
                sysUserMenu.setCreateTime(new Date());
                sysUserMenu.setMenuId(s);
                sysUserMenu.setUserId(sysUserRoleDto.getUserId());
                sysUserMenu.setModifier(sysUserRoleDto.getName());
                sysUserMenu.setCreator(sysUserRoleDto.getName());
                listMenu.add(sysUserMenu);
            }
            sysUserMenuMapper.insertList(listMenu);
        }
        return num;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 重置密码(登录密码和交易密码)
     **/
    @Override
    public int resetPassword(String modifier, String userId) {
        SysUser sysUser = sysUserMapper.selectByPrimaryKey(userId);
        if (sysUser == null) {//用户不存在的判断
            throw new BusinessException(EResultEnum.USER_NOT_EXIST.getCode());
        }
        if (StringUtils.isBlank(sysUser.getEmail())) {//重置密码邮箱不能为空的判断
            throw new BusinessException(EResultEnum.USER_EMAIL_IS_NOT_NULL.getCode());
        }
        sysUser.setId(userId);
        //随机生成六位数密码
        String pwd = this.newPwd();
        sysUser.setPassword(encryptPassword(pwd));//登录密码
        //随机生成六位交易密码
        String twd = this.newPwd();
        sysUser.setTradePassword(encryptPassword(twd));//交易密码
        sysUser.setModifier(modifier);
        //重置密码后邮件告知用户
        Map<String, Object> map = new HashMap<String, Object>();
        String time = DateUtil.getCurrentDate() + " " + DateUtil.getCurrentTime();
        map.put("date", time);
        map.put("pwd", pwd);
        map.put("twd", twd);
        messageFeign.sendTemplateMail(sysUser.getEmail(), auditorProvider.getLanguage(), Status._0, map);//发送邮件
        return sysUserMapper.updateByPrimaryKeySelective(sysUser);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 修改密码
     **/
    @Override
    public int updatePassword(String modifier, String userId, String oldPassword, String password) {
        if (passwordEncoder.matches(oldPassword, sysUserMapper.getUserPassword(userId))) {
            SysUser sysUser = new SysUser();
            sysUser.setId(userId);
            sysUser.setPassword(encryptPassword(password));
            sysUser.setUpdateTime(new Date());
            sysUserMapper.updateByPrimaryKeySelective(sysUser);
        } else {
            throw new BusinessException(EResultEnum.ORIGINAL_PASSWORD_ERROR.getCode());
        }
        return 0;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 修改交易密码
     **/
    @Override
    public int updateTradePassword(String modifier, String userId, String oldPassword, String password) {
        if (passwordEncoder.matches(oldPassword, sysUserMapper.getTradPassword(userId))) {
            SysUser sysUser = new SysUser();
            sysUser.setId(userId);
            sysUser.setTradePassword(encryptPassword(password));
            sysUser.setUpdateTime(new Date());
            sysUserMapper.updateByPrimaryKeySelective(sysUser);
        } else {
            throw new BusinessException(EResultEnum.TRADE_PASSWORD_ERROR.getCode());
        }
        return 0;
    }

    /**
     * 创建6随机位数字密码
     *
     * @return 6随机位数字密码
     */
    private String newPwd() {
        return IDS.randomNumber(6);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/2/28
     * @Descripate 校验密码
     **/
    @Override
    public Boolean checkPassword(String oldPassword, String password) {
        return passwordEncoder.matches(oldPassword, password);
    }

    /**
     * 解密密码
     *
     * @param password
     */
    @Override
    public String decryptPassword(String password) {
        Attestation attestation = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_PLATFORM_KEY), Attestation.class);
        if (attestation == null) {
            attestation = attestationMapper.selectPlatformPub();
            redisService.set(AsianWalletConstant.ATTESTATION_CACHE_PLATFORM_KEY, JSON.toJSONString(attestation));
        }
        String pw;
        try {
            pw = RSAUtils.decryptByPriKey(password, attestation.getPrikey());
        } catch (Exception e) {
            pw = password;
            log.info("---------------【密码解密错误】---------------");
        }
        return pw;
    }

    /**
     * 机构开户后发送邮件
     *
     * @param institutionDTO
     */
    @Override
    public void openAccountEamin(InstitutionDTO institutionDTO) {
        log.info("*********************开户发送邮件 Start*************************************");
        try {
            if (!StringUtils.isEmpty(institutionDTO.getInstitutionEmail())) {
                log.info("*******************发送的机构邮箱是：*******************", institutionDTO.getInstitutionEmail());
                Map<String, Object> map = new HashMap<>();
                SimpleDateFormat sf = new SimpleDateFormat("yyyy.MM.dd");//日期格式
                map.put("dateTime", sf.format(new Date()));//发送日期
                map.put("institutionName", institutionDTO.getCnName());//机构名称
                map.put("institutionCode", institutionDTO.getInstitutionCode());//机构code
                messageFeign.sendTemplateMail(institutionDTO.getInstitutionEmail(), institutionDTO.getLanguage(), Status._3, map);
            }
        } catch (Exception e) {
            log.error("开户发送邮件失败：{}==={}", institutionDTO.getInstitutionEmail(), e.getMessage());
        }
        log.info("*********************开户发送邮件 End*************************************");
    }

}
