package com.payment.institution.service;

import com.payment.common.base.BaseService;
import com.payment.common.dto.ExportAgencyInstitutionDTO;
import com.payment.common.dto.InstitutionDTO;
import com.payment.common.dto.InstitutionExportDTO;
import com.payment.common.dto.QueryAgencyInstitutionDTO;
import com.payment.common.entity.Institution;
import com.payment.common.vo.*;
import com.payment.institution.entity.InstitutionAudit;
import com.payment.institution.entity.InstitutionHistory;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface InstitutionService extends BaseService<Institution> {


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 添加机构信息
     **/
    String addInstitution(SysUserVO sysUserVO, InstitutionDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 修改机构信息
     **/
    int updateInstitution(SysUserVO sysUserVO, InstitutionDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 查询机构信息
     **/
    PageInfo<Institution> pageFindInstitution(InstitutionDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构信息列表
     **/
    PageInfo<InstitutionAudit> pageFindInstitutionAduit(InstitutionDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 分页查询机构历史记录信息列表
     **/
    PageInfo<InstitutionHistory> pageFindInstitutionHistory(InstitutionDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构Code查询机构信息详情
     **/
    InstitutionAccountVO getInstitutionInfoByCode(String institutionCode);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构Id查询机构信息详情
     **/
    InstitutionDetailVO getInstitutionInfo(String id);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构Id查询机构变更信息详情
     **/
    InstitutionDetailVO getInstitutionHistoryInfo(String id);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/28
     * @Descripate 根据机构Id查询机构审核信息详情
     **/
    InstitutionDetailVO getInstitutionInfoAudit(String id);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/25
     * @Descripate 启用禁用机构
     **/
    int banInstitution(String modifier, String institutionId, Boolean enabled);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 审核机构信息
     **/
    int auditInstitution(String modifier, String institutionId, Boolean enabled, String remark);


    /**
     * 导出功能
     *
     * @param institutionDTO
     * @return
     */
    List<InstitutionExportVO> exportInformation(InstitutionExportDTO institutionDTO);

    /**
     * 通过机构名称模糊查询查询机构信息
     *
     * @param institutionDTO
     * @return
     */
    List<Institution> getInstitutionInfoByName(InstitutionDTO institutionDTO);

    /**
     * 根据代理机构code等查询代理机构相关信息
     *
     * @param institutionDTO
     * @return
     */
    List<Institution> getAgencyList(InstitutionDTO institutionDTO);

    /**
     * 代理商商户信息查询
     *
     * @param queryAgencyInstitutionDTO 代理商商户信息查询DTO
     * @return AgencyInstitutionVO
     */
    PageInfo<AgencyInstitutionVO> getAgencyInstitution(QueryAgencyInstitutionDTO queryAgencyInstitutionDTO);

    /**
     * 代理商商户信息导出
     *
     * @param exportAgencyInstitutionDTO 代理商商户信息导出DTO
     * @return AgencyInstitutionVO
     */
    List<AgencyInstitutionVO> exportAgencyInstitution(ExportAgencyInstitutionDTO exportAgencyInstitutionDTO);
}
