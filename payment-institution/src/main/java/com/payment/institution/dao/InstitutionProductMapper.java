package com.payment.institution.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.dto.InstitutionProductDTO;
import com.payment.common.dto.InstitutionProductExportDTO;
import com.payment.common.vo.InstitutionProductVO;
import com.payment.institution.entity.InstitutionProduct;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InstitutionProductMapper extends BaseMapper<InstitutionProduct> {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 分页查询机构产品信息
     **/
    List<InstitutionProductVO> pageFindInsProduct(InstitutionProductDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 分页查询机构产品信息
     *
     * @param institutionProductDto*/
    List<InstitutionProductVO> exportInsProduct(InstitutionProductExportDTO institutionProductDto);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据产品Id查询产品详情
     **/
    InstitutionProductVO getInsProductById(@Param("insProductId") String insProductId, @Param("language") String language);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据机构号Code查询所有产品
     **/
    List<InstitutionProductVO> selectProductByInsCode(@Param("language") String language, @Param("institutionCode") String institutionCode);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/11
     * @Descripate 更新基础产品限额同步更新机构产品
     **/
    List<InstitutionProduct> updateLimit(@Param("productId") String productId, @Param("limitAmount") BigDecimal limitAmount, @Param("dailyTradingCount") int dailyTradingCount, @Param("dailyTotalAmount") BigDecimal dailyTotalAmount);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/8
     * @Descripate 查询机构是否已分配产品
     **/
    @Select("select count(1) from institution_product where institution_id = #{institutionId} and product_id = #{productId}")
    int selectCountbyInsIdProId(@Param("institutionId") String institutionId, @Param("productId") String productId);

    /**
     * //根据机构id与产品id查询机构产品中间表id
     *
     * @param institutionId
     * @return
     */
    String selectByInstitutionIdAndProductId(@Param("institutionId") String institutionId, @Param("productId") String productId);

    /**
     * 根据机构id和产品id获取机构产品信息
     * @param institutionId
     * @param productId
     * @return
     */
    InstitutionProduct getInstitutionProductByInstitutionIdAndProductId(@Param("institutionId") String institutionId, @Param("productId") String productId);

}
