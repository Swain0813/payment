package com.payment.finance.service;

import com.payment.common.dto.SearchAccountCheckDTO;
import com.payment.finance.entity.CheckAccount;
import com.payment.finance.entity.CheckAccountAudit;
import com.payment.finance.entity.CheckAccountLog;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @description: 对账业务接口
 * @author: XuWenQi
 * @create: 2019-03-26 10:10
 **/
public interface AccountCheckService {

    /**
     * @param file 上传文件
     * @return
     */
    Object ad3ChannelAccountCheck(MultipartFile file);

    /**
     * 差错处理
     *
     * @param
     * @return
     */
    int updateCheckAccount(String checkAccountId, String remark);

    /**
     * 差错复核
     *
     * @param
     * @return
     */
    int auditCheckAccount(String checkAccountId,Boolean enable,String remark);

    /**
     * @param searchAccountCheckDTO 分页查询对账管理
     * @return
     */
    PageInfo<CheckAccountLog> pageAccountCheckLog(SearchAccountCheckDTO searchAccountCheckDTO);


    /**
     * @param searchAccountCheckDTO 分页查询对账管理详情
     * @return
     */
    PageInfo<CheckAccount> pageAccountCheck(SearchAccountCheckDTO searchAccountCheckDTO);

    /**
     * @param searchAccountCheckDTO 分页查询对账管理复核详情
     * @return
     */
    PageInfo<CheckAccountAudit> pageAccountCheckAudit(SearchAccountCheckDTO searchAccountCheckDTO);

    /**
     * @param searchAccountCheckDTO 导出对账管理复核详情
     * @return
     */
    List<CheckAccountAudit> exportAccountCheckAudit(SearchAccountCheckDTO searchAccountCheckDTO);


    /**
     * @param searchAccountCheckDTO 导出对账管理详情
     * @return
     */
    List<CheckAccount> exportAccountCheck(SearchAccountCheckDTO searchAccountCheckDTO);

}
