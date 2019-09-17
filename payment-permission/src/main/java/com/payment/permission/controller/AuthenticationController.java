package com.payment.permission.controller;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.entity.Attestation;
import com.payment.common.entity.Institution;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.*;
import com.payment.common.vo.SysMenuVO;
import com.payment.common.vo.SysRoleVO;
import com.payment.common.vo.SysUserVO;
import com.payment.permission.dao.AttestationMapper;
import com.payment.permission.dao.DeviceBindingMapper;
import com.payment.permission.dao.InstitutionMapper;
import com.payment.permission.dto.TerminalLoginDto;
import com.payment.permission.service.SysUserVoService;
import com.payment.permission.utils.SpringSecurityUser;
import com.payment.permission.utils.TokenUtils;
import com.payment.permission.vo.TerminalLoginVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@Api(description = "权限接口")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private SysUserVoService sysUserVoService;

    @Autowired
    private DeviceBindingMapper deviceBindingMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private InstitutionMapper institutionMapper;

    @Autowired
    private AttestationMapper attestationMapper;

    @Value("${security.jwt.token_expire_hour}")
    private int time;

    @ApiOperation(value = "运维商户后台登录")
    @RequestMapping(method = RequestMethod.POST, path = "/login")
    public BaseResponse login(@RequestBody AuthenticationRequest request)
            throws AuthenticationException {
        // Perform the authentication
        String institutionCode = request.getInstitutionCode();
        String username = request.getUsername();
        if (StringUtils.isNotBlank(institutionCode)) {
            username = (institutionCode + request.getUsername()).trim();
            //获取机构信息
            Institution institution = this.getInstitutionInfo(institutionCode);
            if (institution == null) {//机构信息不存在
                throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());//机构信息不存在
            }
            //机构已禁用
            if (!institution.getEnabled()) {
                throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());//机构已禁用
            }
        }
        SysUserVO sysUserVO = sysUserVoService.getSysUser(username);
        if (sysUserVO == null) {
            throw new BusinessException(EResultEnum.USER_NOT_EXIST.getCode());
        }
        //排除POS机和代理系统
        if (sysUserVO.getType() == AsianWalletConstant.POS || sysUserVO.getType()==AsianWalletConstant.AGENCY) {
            throw new BusinessException(EResultEnum.PERMISSION_NOT_FULL.getCode());
        }
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        request.getPassword()
                )
        );
        //公钥
        Attestation attestation = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_PLATFORM_KEY), Attestation.class);
        if (attestation == null) {
            attestation = attestationMapper.selectPlatformPub();
            redisService.set(AsianWalletConstant.ATTESTATION_CACHE_PLATFORM_KEY, JSON.toJSONString(attestation));
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AuthenticationResponse response = getAuthenticationResponse(username);
        //公钥
        response.setPublicKey(attestation.getPubkey());
        if (StringUtils.isNotBlank(response.getToken())) {
            redisService.set(response.getToken(), JSON.toJSONString(sysUserVO), time * 60 * 60);
        }
        return ResultUtil.success(response);
    }

    /**
     * pos机登录
     * @param request
     * @return
     * @throws AuthenticationException
     */
    @ApiOperation(value = "pos机登录")
    @RequestMapping(method = RequestMethod.POST, path = "/posLogin")
    public BaseResponse posLogin(@RequestBody AuthenticationRequest request)
            throws AuthenticationException {
        // Perform the authentication
        String institutionCode = request.getInstitutionCode();
        String imei = request.getImei();
        if (StringUtils.isBlank(imei) || StringUtils.isBlank(institutionCode)) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //获取机构信息
        Institution institution = this.getInstitutionInfo(institutionCode);
        if (institution == null) {//机构信息不存在
            throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());//机构信息不存在
        }
        //机构已禁用
        if (!institution.getEnabled()) {
            throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());//机构已禁用
        }
        if (!(deviceBindingMapper.selectCountByCodeAndImei(institutionCode.trim(), imei.trim()) == 1)) {
            throw new BusinessException(EResultEnum.DEVICE_NOT_AVAILABLE.getCode());
        }
        String username = request.getUsername();
        if (StringUtils.isNotBlank(institutionCode)) {
            username = (institutionCode + request.getUsername()).trim();
        }
        SysUserVO sysUserVO = sysUserVoService.getSysUser(username);
        if (sysUserVO == null) {
            throw new BusinessException(EResultEnum.USER_NOT_EXIST.getCode());
        }
        if (sysUserVO.getType() != AsianWalletConstant.POS) {
            throw new BusinessException(EResultEnum.PERMISSION_NOT_FULL.getCode());
        }
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AuthenticationResponse response = getAuthenticationResponse(username);
        if (StringUtils.isNotBlank(response.getToken())) {
            //redisService.set(response.getToken(), JSON.toJSONString(sysUserVO), time * 60 * 60);
            redisService.set(response.getToken(), JSON.toJSONString(sysUserVO));
        }
        return ResultUtil.success(response);
    }

    //    @RequestMapping(method = RequestMethod.POST, path = "/register")
    //    public Payload register(@RequestBody AuthenticationRequest request)
    //            throws AuthenticationException {
    //
    //        SysUser user = sysUserService.addUser(request.systemCode, request.getUsername(),
    //                request.getPassword());
    //
    //        return Payload.data(user);
    //    }
    //
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    public BaseResponse refresh(HttpServletRequest request) {
        String token = request.getHeader(AsianWalletConstant.tokenHeader);
        String username = this.tokenUtils.getUsernameFromToken(token);
        SpringSecurityUser user = (SpringSecurityUser) this.sysUserVoService.loadUserByUsername(username);
        if (this.tokenUtils.canTokenBeRefreshed(token, user.getLastPasswordReset())) {
            String refreshedToken = this.tokenUtils.refreshToken(token);
            redisService.set(refreshedToken, JSON.toJSONString(user.getSysUser()), time * 60 * 60);
            redisService.set(token, JSON.toJSONString(user.getSysUser()), 1);
            return ResultUtil.success(refreshedToken);
        } else {
            return ResultUtil.success("刷新token错误");
        }
    }

    //    @RequestMapping(value = "/me", method = RequestMethod.GET)
    //    public Payload me(HttpServletRequest request) {
    //        String token = request.getHeader(Constants.tokenHeader);
    //        String username = this.tokenUtils.getUsernameFromToken(token);
    //
    //        AuthenticationResponse response = getAuthenticationResponse(username);
    //        return Payload.data(response);
    //    }

    private AuthenticationResponse getAuthenticationResponse(String username) {
        UserDetails userDetails = this.sysUserVoService.loadUserByUsername(username);
        SysUserVO sysUser = this.sysUserVoService.getSysUser(username);
        String token = this.tokenUtils.generateToken(userDetails);
        AuthenticationResponse response = new AuthenticationResponse(token);
        response.setUserId(sysUser.getId());
        response.setInstitutionId(sysUser.getInstitutionId());
        response.setUsername(sysUser.getUsername());
        response.setName(sysUser.getName());
        List<ResRole> roles = Lists.newArrayList();
        Set<ResPermissions> permissions = Sets.newHashSet();
        for (SysRoleVO sysRoleVO : sysUser.getRole()) {
            ResRole resRole = new ResRole();
            if (StringUtils.isNotBlank(sysRoleVO.getRoleName())) {
                BeanUtils.copyProperties(sysRoleVO, resRole);
                roles.add(resRole);
            }
            for (SysMenuVO sysMenuVO : sysRoleVO.getMenus()) {
                ResPermissions resPermissions = new ResPermissions();
                BeanUtils.copyProperties(sysMenuVO, resPermissions);
                permissions.add(resPermissions);
            }
        }
        response.setRole(roles);
        response.setPermissions(permissions);
        return response;
    }


    /**
     * 终端登录操作
     *
     * @param terminalLoginDto
     * @return
     * @throws AuthenticationException
     */
    @ApiOperation(value = "终端登录操作")
    @RequestMapping(method = RequestMethod.POST, path = "/terminalLogin")
    public BaseResponse terminalLogin(@RequestBody TerminalLoginDto terminalLoginDto)
            throws AuthenticationException {
        String institutionCode = terminalLoginDto.getInstitutionId();
        String imei = terminalLoginDto.getTerminalId();
        if (StringUtils.isBlank(imei) || StringUtils.isBlank(institutionCode)) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //获取机构信息
        Institution institution = this.getInstitutionInfo(institutionCode);
        if (institution == null) {//机构信息不存在
            throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());//机构信息不存在
        }
        //机构已禁用
        if (!institution.getEnabled()) {
            throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());//机构已禁用
        }
        if (!(deviceBindingMapper.selectCountByCodeAndImei(institutionCode.trim(), imei.trim()) == 1)) {
            throw new BusinessException(EResultEnum.DEVICE_NOT_AVAILABLE.getCode());
        }
        String username = terminalLoginDto.getOperatorId();
        if (StringUtils.isNotBlank(institutionCode)) {
            username = (institutionCode + terminalLoginDto.getOperatorId()).trim();
        }
        SysUserVO sysUserVO = sysUserVoService.getSysUser(username);
        if (sysUserVO == null) {
            throw new BusinessException(EResultEnum.USER_NOT_EXIST.getCode());
        }
        if (sysUserVO.getType() != AsianWalletConstant.POS) {
            throw new BusinessException(EResultEnum.PERMISSION_NOT_FULL.getCode());
        }
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        terminalLoginDto.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AuthenticationResponse response = getAuthenticationResponse(username);
        //创建终端登录输出参数实体
        TerminalLoginVO terminalLoginVO = new TerminalLoginVO();
        if (StringUtils.isNotBlank(response.getToken())) {
            terminalLoginVO.setToken(response.getToken());//设置返回值token
            redisService.set(response.getToken(), JSON.toJSONString(sysUserVO));
        }
        return ResultUtil.success(terminalLoginVO);
    }

    /**
     * 代理系统登录用
     * @param request
     * @return
     * @throws AuthenticationException
     */
    @ApiOperation(value = "代理系统登录")
    @RequestMapping(method = RequestMethod.POST, path = "/agencyLogin")
    public BaseResponse agencyLogin(@RequestBody AuthenticationRequest request)
            throws AuthenticationException {
        String institutionCode = request.getInstitutionCode();
        String username = request.getUsername();
        if (StringUtils.isNotBlank(institutionCode)) {
            username = (institutionCode + request.getUsername()).trim();
            //获取代理机构信息
            Institution institution = this.getInstitutionInfo(institutionCode);
            if (institution == null) {
                //代理机构信息不存在
                throw new BusinessException(EResultEnum.AGENCY_INSTITUTION_NOT_EXIST.getCode());
            }
            //代理机构已禁用
            if (!institution.getEnabled()) {
                throw new BusinessException(EResultEnum.AGENCY_INSTITUTION_IS_DISABLE.getCode());
            }
        }
        SysUserVO sysUserVO = sysUserVoService.getSysUser(username);
        if (sysUserVO == null) {
            throw new BusinessException(EResultEnum.USER_NOT_EXIST.getCode());
        }
        //必须是代理机构才能登录
        if (sysUserVO.getType() != AsianWalletConstant.AGENCY) {
            throw new BusinessException(EResultEnum.PERMISSION_NOT_FULL.getCode());
        }
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AuthenticationResponse response = getAuthenticationResponse(username);
        if (StringUtils.isNotBlank(response.getToken())) {
            redisService.set(response.getToken(), JSON.toJSONString(sysUserVO), time * 60 * 60);
        }
        return ResultUtil.success(response);
    }

    /**
     * 根据机构code获取机构名称
     *
     * @param institutionCode
     * @return
     */
    private Institution getInstitutionInfo(String institutionCode) {
        //查询机构信息,先从redis获取
        Institution institution = JSON.parseObject(redisService.get(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institutionCode)), Institution.class);
        if (institution == null) {
            //redis不存在,从数据库获取
            institution = institutionMapper.selectByCode(institutionCode);
            if (institution == null) {
                //机构信息不存在
                return null;
            }
            //同步redis
            redisService.set(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institution.getInstitutionCode()), JSON.toJSONString(institution));
        }
        return institution;
    }

}
