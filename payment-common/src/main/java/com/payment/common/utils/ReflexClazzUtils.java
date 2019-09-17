/**
 * @createTime: 2018年7月22日 下午9:40:58
 * @copyright: 上海投嶒网络技术有限公司
 */
package com.payment.common.utils;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.OrdersDTO;
import com.payment.common.entity.Orders;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Wu, Hua-Zheng
 * @version v1.0.0
 * @classDesc: 功能描述: 反射工具类
 * @createTime 2018年7月22日 下午9:40:58
 * @copyright: 上海众哈网络技术有限公司
 */
@Slf4j
public class ReflexClazzUtils {

    /**
     * @methodDesc: 功能描述: 获取属性值字段和注释
     * @author Wu, Hua-Zheng
     * @createTime 2018年7月22日 下午11:16:23
     * @version v1.0.0
     */
    public static String getAllFiledItems(Class<?> targetClass, Object target) {

        StringBuffer sb = new StringBuffer();
        Field[] fields = targetClass.getDeclaredFields();
        ApiModelProperty apiModel = null;
        for (Field field : fields) {
            apiModel = field.getAnnotation(ApiModelProperty.class); // 获取指定类型注解

            if (ValidatorToolUtils.isNullOrEmpty(apiModel) || ValidatorToolUtils.isNullOrEmpty(apiModel.value())) {
                continue;
            }

            // 添加字段注释和属性字段名称
            sb.append("【" + apiModel.value() + "(" + field.getName());
            try {
                // 增加属性值
                sb.append(")】----------<" + String.valueOf(field.get(target)) + ">\r\n");
            } catch (IllegalArgumentException e) {
                log.error("[ReflexClazzUtils-getAllFiledItems异常]-{}", e);
            } catch (IllegalAccessException e) {
                log.error("[ReflexClazzUtils-getAllFiledItems异常]-{}", e);
            }

        }
        return sb.toString();
    }


    /**
     * @methodDesc: 功能描述: 获取属性值字段和注释
     * @author Wu, Hua-Zheng
     * @createTime 2018年7月22日 下午11:16:23
     * @version v1.0.0
     */
    public static Map<String, String[]> getFiledStructMap(Class<?> targetClass) {

        Map<String, String[]> result = new HashMap<String, String[]>();
        List<String> fieldList = new LinkedList<String>();
        List<String> commentList = new LinkedList<String>();

        Field[] fields = targetClass.getDeclaredFields();
        ApiModelProperty apiModel = null;
        for (Field field : fields) {
            apiModel = field.getAnnotation(ApiModelProperty.class); // 获取指定类型注解

            if (ValidatorToolUtils.isNullOrEmpty(apiModel) || ValidatorToolUtils.isNullOrEmpty(apiModel.value())) {
                continue;
            }
            // 添加字段注释和属性字段名称
            //commentList.add(apiModel.value() + "[" + field.getName() + "]");
            commentList.add(apiModel.value());
            fieldList.add(field.getName());
        }

        String[] titleArray = new String[commentList.size()];
        titleArray = commentList.toArray(titleArray);

        String[] attrArray = new String[commentList.size()];
        attrArray = fieldList.toArray(attrArray);

        //字段
        result.put(AsianWalletConstant.EXCEL_ATTRS, attrArray);

        //注释
        result.put(AsianWalletConstant.EXCEL_TITLES, titleArray);

        return result;
    }


    /**
     * @methodDesc: 功能描述: 获取属性值字段和注释
     * @author Wu, Hua-Zheng
     * @createTime 2018年7月22日 下午11:16:23
     * @version v1.0.0
     */
    public static Object setFiledValue(String fieldName, String value, Object obj) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            //设置对象的访问权限，保证对private的属性的访问
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[ReflexClazzUtils-setFiledValue异常]-{}", e);
        }
        return obj;
    }


    /**
     * 根据属性名获取属性值
     *
     * @param fieldName
     * @param object
     * @return
     */
    public String getFieldValueByFieldName(String fieldName, Object object) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            //设置对象的访问权限，保证对private的属性的访问
            field.setAccessible(true);
            return (String) field.get(object);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[ReflexClazzUtils-getFieldValueByFieldName异常]-{}", e);
            return null;
        }
    }

    /**
     * 根据属性名获取属性元素，包括各种安全范围和所有父类
     *
     * @param fieldName
     * @param object
     * @return
     */
    public Field getFieldByClasss(String fieldName, Object object) {
        Field field = null;
        Class<?> clazz = object.getClass();

        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (Exception e) {
                // 这里甚么都不能抛出去。
                // 如果这里的异常打印或者往外抛，则就不会进入
                e.printStackTrace();
                log.error("[ReflexClazzUtils-getFieldByClasss异常]-{}", e);
                return null;

            }
        }
        return field;

    }

    /**
     * 获取对象中的属性名与属性值
     *
     * @param f 对象
     * @return 属性名与属性值对应的Map
     */
    public static Map<String, Object> getFieldNames(Object f) {
        Map<String, Object> map = new LinkedHashMap<>();
        // 获取f对象对应类中的所有属性域
        Field[] fields = f.getClass().getDeclaredFields();
        Field[] declaredFields = f.getClass().getSuperclass().getDeclaredFields();

        for (int i = 0, len = fields.length; i < len; i++) {
            try {
                // 对于每个属性，获取属性名
                String varName = fields[i].getName();
                // 获取原来的访问控制权限
                boolean accessFlag = fields[i].isAccessible();
                // 修改访问控制权限
                fields[i].setAccessible(true);
                // 获取在对象f中属性fields[i]对应的对象中的变量
                Object obj = fields[i].get(f);
                map.put(varName, obj);
                // 恢复访问控制权限
                fields[i].setAccessible(accessFlag);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        for (int i = 0, len = declaredFields.length; i < len; i++) {
            try {
                // 对于每个属性，获取属性名
                String varName = declaredFields[i].getName();
                // 获取原来的访问控制权限
                boolean accessFlag = declaredFields[i].isAccessible();
                // 修改访问控制权限
                declaredFields[i].setAccessible(true);
                // 获取在对象f中属性fields[i]对应的对象中的变量
                Object obj = declaredFields[i].get(f);
                map.put(varName, obj);
                // 恢复访问控制权限
                declaredFields[i].setAccessible(accessFlag);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return map;
    }

    public static Map<String, Object> getCommonFieldNames(Object f) {
        Map<String, Object> map = new LinkedHashMap<>();
        // 获取f对象对应类中的所有属性域
        Field[] fields = f.getClass().getDeclaredFields();
        for (int i = 0, len = fields.length; i < len; i++) {
            try {
                // 对于每个属性，获取属性名
                String varName = fields[i].getName();
                // 获取原来的访问控制权限
                boolean accessFlag = fields[i].isAccessible();
                // 修改访问控制权限
                fields[i].setAccessible(true);
                // 获取在对象f中属性fields[i]对应的对象中的变量
                Object obj = fields[i].get(f);
                map.put(varName, obj);
                // 恢复访问控制权限
                fields[i].setAccessible(accessFlag);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return map;
    }

    /**
     * 获取对象中的空值属性
     *
     * @param source
     * @return
     */
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static void main(String[] args) {
        OrdersDTO ordersDTO = new OrdersDTO();
        ordersDTO.setInstitutionCode("111");
        Orders orders = new Orders();

        long start = System.currentTimeMillis();
        //BeanUtils.copyProperties(ordersDTO, orders, ReflexClazzUtils.getNullPropertyNames(ordersDTO));
        orders.setTradeType((byte)0);
        orders.setTradeDirection((byte)0);
        orders.setInstitutionCode("");
        orders.setInstitutionName("");
        orders.setSecondInstitutionName("");
        orders.setSecondInstitutionCode("");
        orders.setLanguage("");
        orders.setInstitutionOrderTime(new Date());
        orders.setInstitutionOrderId("");
        orders.setAmount(new BigDecimal("0"));
        orders.setOrderCurrency("");
        orders.setCommodityName("");
        orders.setReturnUrl("");
        orders.setJumpUrl("");
        orders.setProductCode(0);
        orders.setProductName("");
        orders.setChannelCode("");
        orders.setChannelName("");
        orders.setDeviceCode("");
        orders.setDeviceOperator("");
        orders.setExchangeRate(new BigDecimal("0"));
        orders.setExchangeTime(new Date());
        orders.setExchangeStatus((byte)0);
        orders.setTradeCurrency("");
        orders.setTradeAmount(new BigDecimal("0"));
        orders.setTradeStatus((byte)0);
        orders.setRefundStatus((byte)0);
        orders.setCancelStatus((byte)0);
        orders.setChannelNumber("");
        orders.setClearStatus((byte)0);
        orders.setSettleStatus((byte)0);
        orders.setRateType("");
        orders.setRate(new BigDecimal("0"));
        orders.setFee(new BigDecimal("0"));
        orders.setFeePayer((byte)0);
        orders.setChargeStatus((byte)0);
        orders.setChargeTime(new Date());
        orders.setPayMethod("");
        orders.setReqIp("");
        orders.setReportNumber("");
        orders.setReportChannelTime(new Date());
        orders.setChannelCallbackTime(new Date());
        orders.setFloatRate(new BigDecimal("0"));
        orders.setAddValue(new BigDecimal("0"));
        orders.setGoodsDescription("");
        orders.setDraweeName("");
        orders.setDraweeAccount("");
        orders.setDraweeBank("");
        orders.setDraweeEmail("");
        orders.setDraweePhone("");
        orders.setSign("");
        orders.setRemark1("");
        orders.setRemark2("");
        orders.setRemark3("");
        orders.setChannelFee(new BigDecimal("0"));
        orders.setChannelRate(new BigDecimal("0"));
        orders.setChannelFeeType("");
        orders.setChannelGatewayRate(new BigDecimal("0"));
        orders.setChannelGatewayFee(new BigDecimal("0"));
        orders.setChannelGatewayFeeType("");
        orders.setChannelGatewayCharge((byte)0);
        orders.setChannelGatewayStatus((byte)0);
        orders.setProductSettleCycle("");
        orders.setIssuerId("");
        orders.setId("");
        orders.setCreateTime(new Date());
        orders.setUpdateTime(new Date());
        orders.setCreator("");
        orders.setModifier("");
        orders.setRemark("");

        orders.setId("1");
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
