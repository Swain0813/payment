package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.ProductSearchDTO;
import com.payment.common.dto.ProductSearchExportDTO;
import com.payment.common.entity.Product;
import com.payment.common.vo.ExportProductVO;
import com.payment.common.vo.ProductVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ProductMapper  extends BaseMapper<Product> {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 根据支付方式查询所有产品
     **/
    @Select("select  p.id as id ,concat(d.name,'-',p.currency) as payType from product p, dictionary d where pay_type = #{payType} and p.pay_type = d.`code`  and d.language =#{language} ")
    List<Product> selectProductByPayType(@Param("payType") String payType,@Param("language") String language);

    List<ProductVO> getAllProduct( ProductSearchDTO productSearchDTO);

    Product getProductById(@Param("id") String id);

    /**
     * @Author YangXu
     * @Date 2019/3/1
     * @Descripate 查询条数
     * @return
     **/
    @Select("select count(1) from product")
    int getCount();


    /**
     * @return
     * @Author YangXu
     * @Date 2019/1/29
     * @Descripate 更新产品
     **/
    @Update("update product set product_code = #{code},product_img = #{img},modifier = #{modifier},update_time = #{date} where product_code = #{code}")
    int updateImg(@Param("code") Integer code, @Param("img") String img, @Param("modifier") String modifier, @Param("date") Date date);

    List<ProductVO>  pageProduct(ProductSearchDTO productSearchDTO);

    /**
     * @return
     * @Author XuWenQi
     * @Date 2019/5/24
     * @Descripate 导出产品信息
     **/
    List<ExportProductVO> exportProduct(ProductSearchExportDTO productSearchDTO);
}
