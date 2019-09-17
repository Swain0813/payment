package com.payment.institution.service.impl;
import com.payment.common.base.BaseServiceImpl;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.PaymentModeDTO;
import com.payment.common.entity.Dictionary;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.IDS;
import com.payment.common.vo.PaymentModeVO;
import com.payment.institution.dao.DictionaryMapper;
import com.payment.institution.dao.PaymentModeMapper;
import com.payment.institution.entity.PaymentMode;
import com.payment.institution.service.PaymentModeService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

/**
 * @author shenxinran
 * @Date: 2019/3/3 13:47
 * @Description: 支付管理业务实现类
 */
@Slf4j
@Service
@Transactional
public class PaymentModeServiceImpl extends BaseServiceImpl<PaymentMode> implements PaymentModeService {

    @Autowired
    private PaymentModeMapper paymentModeMapper;

    @Autowired
    private DictionaryMapper dictionaryMapper;


    /**
     * 添加支付方式
     *
     * @param paymentModeDTO
     * @return 1 or 0
     */
    @Override
    public int addPayinfo(PaymentModeDTO paymentModeDTO) {
        //校验参数
        checkParameter(paymentModeDTO);
        //插入的名称不存在时
        if (StringUtils.isBlank(dictionaryMapper.getNameAndLanguage(paymentModeDTO.getPayTypeName(), paymentModeDTO.getLanguage()))) {
            //查出最新的code，自动递增code
            String latestCode = dictionaryMapper.getLatestCodeBydictypeCodeAndLanguage(AsianWalletConstant.PAY_METHOD_CODE, null);
            Integer lastCode=null;
            if (StringUtils.isBlank(latestCode)) {//支付方式不存在的场合
                lastCode = 1;//从1开始
            } else {
                lastCode = Integer.valueOf(latestCode) + 1;
            }
            //更新字典数据
            Dictionary dictionary = new Dictionary();
            dictionary.setCreateTime(new Date());
            dictionary.setDictypeCode(AsianWalletConstant.PAY_METHOD_CODE);
            dictionary.setCreator(paymentModeDTO.getCreator());
            //添加图标
            dictionary.setIcon(paymentModeDTO.getDIcon());
            dictionary.setLanguage(paymentModeDTO.getLanguage());
            dictionary.setEnabled(true);
            dictionary.setId(IDS.uuid2());
            dictionary.setName(paymentModeDTO.getPayTypeName());
            dictionary.setCode(AsianWalletConstant.PAY_METHOD_CODE + "_" + lastCode);
            if (dictionaryMapper.selectByCode(dictionary.getCode()) != null) {
                throw new BusinessException(EResultEnum.REPEATED_ADDITION.getCode());
            }
            if (dictionaryMapper.insertSelective(dictionary) > 0) {
                //更新支付表
                return addDuPayInfo(paymentModeDTO);
            }
        }
        //支付方式已存在
        return addDuPayInfo(paymentModeDTO);
    }


    /**
     * 查询支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @Override
    public PageInfo<PaymentModeVO> pagePayInfo(PaymentModeDTO paymentModeDTO) {
        return new PageInfo(paymentModeMapper.pagePayInfo(paymentModeDTO));
    }

    /**
     * 查询所有支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @Override
    public List<PaymentModeVO> getPayInfo(PaymentModeDTO paymentModeDTO) {
        return paymentModeMapper.getPayInfo(paymentModeDTO);
    }

    /**
     * 启用禁用支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @Override
    public int banPayInfo(PaymentModeDTO paymentModeDTO) {
        PaymentMode paymentMode = new PaymentMode();
        BeanUtils.copyProperties(paymentModeDTO, paymentMode);
        paymentMode.setUpdateTime(new Date());
        return paymentModeMapper.updateByPrimaryKeySelective(paymentMode);
    }


    /**
     * 添加支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    private int addDuPayInfo(PaymentModeDTO paymentModeDTO) {
        PaymentMode paymentMode = new PaymentMode();
        String payType = dictionaryMapper.getNameAndLanguage(paymentModeDTO.getPayTypeName(), paymentModeDTO.getLanguage());
        BeanUtils.copyProperties(paymentModeDTO, paymentMode);
        paymentMode.setIcon(paymentModeDTO.getPIcon());
        paymentMode.setEnabled(true);
        paymentMode.setPayType(payType);
        paymentMode.setCreateTime(new Date());
        if (paymentModeMapper.selectExist(paymentMode.getPayType(), paymentMode.getDealType()) > 0) {
            throw new BusinessException(EResultEnum.PAYMENTMODE_EXIST.getCode());
        }
        return paymentModeMapper.insert(paymentMode);
    }

    /**
     * 添加不同语言的支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @Override
    public int addOtherLanguage(PaymentModeDTO paymentModeDTO) {
        //校验参数
        if (StringUtils.isEmpty(paymentModeDTO.getPayTypeName()) || StringUtils.isEmpty(paymentModeDTO.getLanguage()) || StringUtils.isEmpty(paymentModeDTO.getPayType())) {
            log.info("------------添加支付方式的多语言参数不完整------------");
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //检查类型是否存在
        Dictionary oldPaytype = dictionaryMapper.selectByCode(paymentModeDTO.getPayType());
        if (oldPaytype == null) {
            log.info("------------支付方式不存在------------");
            throw new BusinessException(EResultEnum.DICINFO_TYPE_VALUE_NOT_EXIST.getCode());
        }
        //检查是否重复添加
        if (dictionaryMapper.selectDicCodeAndLanguage(paymentModeDTO.getPayType(), paymentModeDTO.getLanguage()) > 0) {
            log.info("------------重复添加同种支付方式的多语言------------");
            throw new BusinessException(EResultEnum.REPEATED_ADDITION.getCode());
        }
        Dictionary dictionary = new Dictionary();
        dictionary.setCreateTime(new Date());
        dictionary.setDictypeCode(AsianWalletConstant.PAY_METHOD_CODE);
        dictionary.setCreator(paymentModeDTO.getCreator());
        //添加图标
        dictionary.setIcon(oldPaytype.getIcon());
        dictionary.setLanguage(paymentModeDTO.getLanguage());
        dictionary.setEnabled(true);
        dictionary.setId(IDS.uuid2());
        dictionary.setName(paymentModeDTO.getPayTypeName());
        dictionary.setCode(paymentModeDTO.getPayType());
        return dictionaryMapper.insert(dictionary);
    }

    /**
     * 校验参数
     *
     * @param paymentModeDTO
     */
    private void checkParameter(PaymentModeDTO paymentModeDTO) {
        //校验规则
        if (StringUtils.isEmpty(paymentModeDTO.getPayTypeName())) {
            throw new BusinessException(EResultEnum.PAYMENTMODE_ADD_PAYCODE_IS_NULL.getCode());
        }
        if (StringUtils.isEmpty(paymentModeDTO.getDealType())) {
            throw new BusinessException(EResultEnum.PAYMENTMODE_ADD_DEALCODE_IS_NULL.getCode());
        }
        if (StringUtils.isEmpty(paymentModeDTO.getLanguage())) {
            throw new BusinessException(EResultEnum.DICINFO_LANGUAGE_IS_NULL.getCode());
        }
    }

    /**
     * 更新支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @Override
    public int updatePayInfo(PaymentModeDTO paymentModeDTO) {
        PaymentMode paymentMode = new PaymentMode();
        BeanUtils.copyProperties(paymentModeDTO, paymentMode);
        paymentMode.setIcon(paymentModeDTO.getPIcon());
        paymentMode.setUpdateTime(new Date());
        return paymentModeMapper.updateByPrimaryKeySelective(paymentMode);
    }

}
