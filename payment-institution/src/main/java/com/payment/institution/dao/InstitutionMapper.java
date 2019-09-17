package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.ExportAgencyInstitutionDTO;
import com.payment.common.dto.InstitutionDTO;
import com.payment.common.dto.InstitutionExportDTO;
import com.payment.common.dto.QueryAgencyInstitutionDTO;
import com.payment.common.entity.Institution;
import com.payment.common.vo.AgencyInstitutionVO;
import com.payment.common.vo.InstitutionAccountVO;
import com.payment.common.vo.InstitutionDetailVO;
import com.payment.common.vo.InstitutionExportVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstitutionMapper extends BaseMapper<Institution> {

    /**
     * 机构分页查询
     *
     * @param institutionDTO
     * @return
     */
    List<Institution> pageFindInstitution(InstitutionDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/1
     * @Descripate 根据机构Code查询机构信息详情
     **/
    InstitutionAccountVO getInstitutionAccountInfoByCode(String institutionCode);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/1
     * @Descripate 根据机构id查询机构详细信息
     **/
    InstitutionDetailVO getInstitutionDetail(String institutionId);

    /**
     * 导出功能
     *
     * @param institutionDTO
     * @return
     */
    List<InstitutionExportVO> selectExport(InstitutionExportDTO institutionDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/8
     * @Descripate 查询机构名是否存在
     **/
    @Select("select count(1) from institution where cn_name = #{InstitutionName} or en_name = #{InstitutionName}")
    int selectCountByInsName(@Param("InstitutionName") String InstitutionName);

    /**
     * 通过机构名称模糊查询查询机构信息
     *
     * @param institutionDTO
     * @return
     */
    List<Institution> getInstitutionInfoByName(InstitutionDTO institutionDTO);

    /**
     * 根据代理机构code和机构类型等查询代理机构相关信息
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
    List<AgencyInstitutionVO> pageAgencyInstitution(QueryAgencyInstitutionDTO queryAgencyInstitutionDTO);

    /**
     * 代理商商户信息导出
     *
     * @param exportAgencyInstitutionDTO 代理商商户信息导出DTO
     * @return AgencyInstitutionVO
     */
    List<AgencyInstitutionVO> exportAgencyInstitution(ExportAgencyInstitutionDTO exportAgencyInstitutionDTO);

    /**
     * 根据机构code获取机构信息
     * @param institutionCode
     * @return
     */
    Institution getInstitutionByCode(String institutionCode);
}
