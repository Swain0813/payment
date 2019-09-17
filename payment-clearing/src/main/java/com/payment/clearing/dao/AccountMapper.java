package com.payment.clearing.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Account;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccountMapper   extends BaseMapper<Account> {

    /**
     * @Author YangXu
     * @Date 2019/7/25
     * @Descripate 根据机构号币种查询账户
     * @return
     **/
    Account selectByInstitutionIdAndCode(@Param("institutionId") String institutionId, @Param("currency")String currency);

    /**
     * 更新冻结账户余额
     * @param mvafrz
     * @return
     */
    int updateFrozenBalance(Account mvafrz);

    /**
     * @Author YangXu
     * @Date 2019/7/25
     * @Descripate 根据机构号币种查询账户ID
     * @return
     **/
    String selectIdByInstitutionIdAndCode(String merchantid, String sltcurrency);

    /**
     * @Author YangXu
     * @Date 2019/7/26
     * @Descripate 更新商户清算虚拟账户中的资金专用方法
     * @return
     **/
    int updateCTAMTByPrimaryKey(Account mva);

    /**
     * @Author YangXu
     * @Date 2019/7/29
     * @Descripate 更新商户结算虚拟账户中的资金专用方法
     * @return
     **/
    int updateSTAMTByPrimaryKey(Account mva);

    /**
     * @Author YangXu
     * @Date 2019/8/23
     * @Descripate 更新分润资金
     * @return
     **/
    int updateSPAMTByPrimaryKey(Account mva);

    /**
     * 查询账户表中结算账户余额减去冻结金额大于0的账户信息以及关联结算控制信息
     * @return
     */
    List<Account> getAccounts();

    /**
     * 更新机构账户对应币种的结算金额
     * @param mva
     * @return
     */
    int updateAccountByPrimaryKey(Account mva);

}
