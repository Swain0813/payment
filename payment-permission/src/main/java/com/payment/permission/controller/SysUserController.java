package com.payment.permission.controller;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.InstitutionDTO;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResPermissions;
import com.payment.common.response.ResRole;
import com.payment.common.response.ResultUtil;
import com.payment.common.vo.SysMenuVO;
import com.payment.common.vo.SysRoleVO;
import com.payment.common.vo.SysUserDetailVO;
import com.payment.common.vo.SysUserVO;
import com.payment.permission.dto.*;
import com.payment.permission.entity.SysMenu;
import com.payment.permission.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

/**
 * @description: test
 * @author: YangXu
 * @create: 2019-01-07 16:20
 **/
@RestController
@Slf4j
@Api(description = "用户相关接口")
@RequestMapping("/permission")
public class SysUserController extends BaseController {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private SysUserVoService sysUserVoService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private AllotPermissionService allotPermissionService;


    @ApiOperation(value = "查询SysUserDeatil")
    @GetMapping("/getSysUserDeatil")
    public BaseResponse getSysUserDeatil(@RequestParam @ApiParam String userName) {
        SysUserVO sysUser = sysUserVoService.getSysUser(userName);
        SysUserDetailVO sysUserDetailVO = new SysUserDetailVO();
        BeanUtils.copyProperties(sysUser, sysUserDetailVO);
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
        sysUserDetailVO.setRole(roles);
        sysUserDetailVO.setPermissions(permissions);
        return ResultUtil.success(sysUserDetailVO);
    }

    @ApiOperation(value = "机构添加用户")
    @PostMapping("/addSysUserbyInstitution")
    public BaseResponse addSysUserbyInstitution(@RequestBody @ApiParam SysUserRoleDto sysUserRoleDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(sysUserRoleDto),
                "机构添加用户"));
        return ResultUtil.success(sysUserVoService.addSysUserbyInstitution(this.getSysUserVO(), sysUserRoleDto));
    }

    @ApiOperation(value = "运营后台添加修改用户")
    @PostMapping("/addSysUserbyAdmin")
    public BaseResponse addSysUserbyAdmin(@RequestBody @ApiParam SysUserRoleDto sysUserRoleDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(sysUserRoleDto),
                "运营后台添加修改用户"));
        return ResultUtil.success(sysUserVoService.addSysUserbyAdmin(this.getSysUserVO().getUsername(), sysUserRoleDto));
    }


    @ApiOperation(value = "分页查询用户信息")
    @PostMapping("/pageGetSysUser")
    public BaseResponse pageGetSysUser(@RequestBody @ApiParam SysUserSecDto sysUserSecDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(sysUserSecDto),
                "分页查询用户信息"));
        return ResultUtil.success(sysUserService.pageGetSysUser(sysUserSecDto));
    }

    @ApiOperation(value = "分页查询角色信息")
    @PostMapping("/pageGetSysRole")
    public BaseResponse pageGetSysRole(@RequestBody @ApiParam SysRoleSecDto sysRoleSecDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(sysRoleSecDto),
                "分页查询角色信息"));
        return ResultUtil.success(sysUserService.pageGetSysRole(sysRoleSecDto));
    }

    @ApiOperation(value = "添加角色信息并分配权限")
    @PostMapping("/addSysRole")
    public BaseResponse addSysRole(@RequestBody @ApiParam SysRoleMenuDto sysRoleMenuDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(sysRoleMenuDto),
                "添加角色信息并分配权限"));
        return ResultUtil.success(sysRoleService.addSysRole(this.getSysUserVO().getUsername(), sysRoleMenuDto));
    }

    @ApiOperation(value = "添加权限信息")
    @PostMapping("/addMenu")
    public BaseResponse addMenu(@RequestBody @ApiParam SysMenu sysMenu) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(sysMenu),
                "添加权限信息"));
        sysMenu.setCreator(this.getSysUserVO().getUsername());
        return ResultUtil.success(sysMenuService.addMenu(sysMenu));
    }

    @ApiOperation(value = "根据权限ID修改权限")
    @PostMapping("/updateMenuById")
    public BaseResponse updateMenuById(@RequestBody @ApiParam SysMenu sysMenu) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(sysMenu),
                "根据权限ID修改权限"));
        sysMenu.setModifier(this.getSysUserVO().getUsername());
        return ResultUtil.success(sysMenuService.updateMenuById(sysMenu));
    }

    @ApiOperation(value = "查询用户所有权限信息（userId可不传）")
    @GetMapping("/getAllMenuByUserId")
    public BaseResponse getAllMenuByUserId(@RequestParam(required = false) @ApiParam String userId, Integer type) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "查询用户所有权限信息"));
        return ResultUtil.success(sysMenuService.getAllMenu(userId, type));
    }

    @ApiOperation(value = "查询用户角色信息（userId,institutionId可不传）")
    @GetMapping("/getAllRoleByUserId")
    public BaseResponse getAllRoleByUserId(@RequestParam(required = false) @ApiParam String userId, @RequestParam(required = false) @ApiParam String institutionId, @RequestParam(required = false) @ApiParam Integer type) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "查询用户角色信息"));
        return ResultUtil.success(sysRoleService.getAllRole(userId, institutionId, type));
    }

    @ApiOperation(value = "查询角色所有权限信息（roleId可不传）")
    @GetMapping("/getAllMeunByRoleId")
    public BaseResponse getAllMeunByRoleId(@RequestParam(required = false) @ApiParam String roleId, Integer type) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "查询角色所有权限信息"));
        return ResultUtil.success(sysMenuService.getAllMeunByRoleId(roleId, type));
    }

    @ApiOperation(value = "修改用户分配角色")
    @PostMapping("/addUserRole")
    public BaseResponse addUserRole(@RequestBody @ApiParam SysUserRoleDto sysUserRoleDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(sysUserRoleDto),
                "修改用户分配角色"));
        return ResultUtil.success(allotPermissionService.addUserRole(this.getSysUserVO(), sysUserRoleDto));
    }

    @ApiOperation(value = "修改角色分配权限")
    @PostMapping("/addRoleMenu")
    public BaseResponse addRoleMenu(@RequestBody @ApiParam SysRoleMenuDto sysRoleMenuDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(sysRoleMenuDto),
                "修改角色分配权限"));
        return ResultUtil.success(allotPermissionService.addRoleMenu(this.getSysUserVO(), sysRoleMenuDto));
    }

    @ApiOperation(value = "修改用户分配权限")
    @PostMapping("/addUserMenu")
    public BaseResponse addUserMenu(@RequestBody @ApiParam SysUserMenuDto sysUserMenuDto) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(sysUserMenuDto),
                "修改用户分配权限"));
        return ResultUtil.success(allotPermissionService.addUserMenu(this.getSysUserVO(), sysUserMenuDto));
    }

    @ApiOperation(value = "禁用/启用用户")
    @GetMapping("/banUser")
    public BaseResponse banUser(@RequestParam @ApiParam String userId, Boolean enabled) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "禁用/启用用户"));
        return ResultUtil.success(sysUserService.banUser(this.getSysUserVO(), userId, enabled));
    }

    @ApiOperation(value = "禁用/启用角色")
    @GetMapping("/banRole")
    public BaseResponse banRole(@RequestParam @ApiParam String roleId, Boolean enabled,Integer type) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "禁用/启用角色"));
        return ResultUtil.success(sysRoleService.banRole(this.getSysUserVO(), roleId, enabled,type));
    }

    @ApiOperation(value = "禁用/启用权限")
    @GetMapping("/banMenu")
    public BaseResponse banMenu(@RequestParam @ApiParam String menuId, Boolean enabled) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "禁用/启用权限"));
        return ResultUtil.success(sysMenuService.banMenu(this.getSysUserVO(), menuId, enabled));
    }

    @ApiOperation(value = "重置密码")
    @GetMapping("/resetPassword")
    public BaseResponse resetPassword(@RequestParam @ApiParam String userId) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "重置密码"));
        return ResultUtil.success(sysUserVoService.resetPassword(this.getSysUserVO().getUsername(), userId));
    }

    @ApiOperation(value = "修改密码")
    @GetMapping("/updatePassword")
    public BaseResponse updatePassword(@RequestParam @ApiParam String userId, String oldPassword, String password) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "修改密码"));
        return ResultUtil.success(sysUserVoService.updatePassword(this.getSysUserVO().getUsername(), userId, oldPassword, password));
    }

    @ApiOperation(value = "修改交易密码")
    @GetMapping("/updateTradePassword")
    public BaseResponse updateTradePassword(@RequestParam @ApiParam String userId, String oldPassword, String password) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSONObject.toJSONString(this.getRequest().getParameterMap()),
                "修改交易密码"));
        return ResultUtil.success(sysUserVoService.updateTradePassword(this.getSysUserVO().getUsername(), userId, oldPassword, password));
    }


    @ApiOperation(value = "setAdmin")
    @GetMapping("/setAdmin")
    public BaseResponse setAdmin(@RequestParam @ApiParam String roleId, String type) {
        return ResultUtil.success(allotPermissionService.setAdmin(roleId, type));
    }

    @ApiOperation(value = "检查交易密码")
    @GetMapping("/checkPassword")
    public BaseResponse checkPassword(@RequestParam @ApiParam String oldPassword, @RequestParam @ApiParam String password) {
        if (sysUserVoService.checkPassword(oldPassword, password)) {
            return ResultUtil.success("true");
        }
        return ResultUtil.success("false");
    }

    @ApiOperation(value = "发送开户邮件")
    @PostMapping(value = "/sendInstitutionEmail")
    public BaseResponse sendInstitutionEmail(@RequestBody @ApiParam InstitutionDTO institutionDTO) {
        institutionDTO.setLanguage(this.getLanguage());//设置语言
        sysUserVoService.openAccountEamin(institutionDTO);
        return ResultUtil.success();
    }

}
