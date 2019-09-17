package com.payment.clearing.controller;
import com.payment.clearing.constant.Const;
import com.payment.clearing.dao.TcsSysConstMapper;
import com.payment.clearing.service.TransferService;
import com.payment.common.dto.TransferFundDTO;
import com.payment.common.entity.TcsStFlow;
import com.payment.common.response.BaseResponse;
import com.payment.common.utils.MD5;
import com.payment.common.utils.SignTools;
import com.payment.common.vo.TransferFundVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 转账服务
 * @author: YangXu
 * @create: 2019-08-02 13:53
 **/
@Slf4j
@RestController
@Api(description = "转账服务接口")
@RequestMapping("/TransferAccountAction")
public class TransferController {

    @Autowired
    private TcsSysConstMapper tcsSysConstMapper;

    @Autowired
    private TransferService transferService;

    @ApiOperation(value = "转账服务接口")
    @PostMapping("v1/CSTransferAccount")
    public TransferFundVO CSTransferAccount(@RequestBody TransferFundDTO account) {
        TransferFundVO transferFundVO = new TransferFundVO();
        BeanUtils.copyProperties(account, transferFundVO);
        log.info("************** 转账服务 CSTransferAccount ************* #开始，时间：{}", new Date());
        transferFundVO.setRespMsg(Const.Code.FAILED_MSG);
        transferFundVO.setRespCode(Const.Code.FAILED);
        String md5key = tcsSysConstMapper.getCSAPI_MD5Key();//获得交易系统的MD5Key
        BaseResponse message = transferService.verificationAPIInputParamter(account,md5key);
        if (message == null || !message.getCode().equals(Const.Code.OK)) {
            //验证参数失败
            transferFundVO.setRespMsg(message.getMsg());
            log.info("************** 转账服务 CSTransferAccount ************* # 验证输入参数失败，结束，时间：{}" , new Date());
        }
        //先取出封装号的st记录
        Object[] obj = (Object[]) message.getData();
        if (obj == null || obj.length < 2) {
            //返回参数为空
            transferFundVO.setRespMsg(Const.Code.MSG_GetResponseFail + ":获取返回参数为空");
            log.info("************** 转账服务 CSTransferAccount ************* # 获取验证方法返回参数为空，时间：{}" , new Date());
        }
        TcsStFlow outst = (TcsStFlow) obj[0];
        TcsStFlow inst = (TcsStFlow) obj[1];
        if (outst == null || inst == null) {
            //outst或者inst为空
            transferFundVO.setRespMsg(Const.Code.MSG_GetResponseFail + ":获取返回参数outst或者inst为空");
            log.info("out ==>com.cscenter.module.CSAPI.Action.TransferAccountAction.CSTransferAccount#获取验证方法返回参数outst或者inst为空，时间：{}" , new Date());
        }
        BaseResponse message2 = transferService.stTransferAccount(account,outst, inst);
        if(message2!=null&&message2.getCode().equals(Const.Code.OK)){
            //转账成功
            Map<String, String> m2=new HashMap<String, String>();
            transferFundVO.setRespCode("T000");
            m2.put("respCode", "T000");
            transferFundVO.setRespMsg("success");
            m2.put("respMsg", "success");
            m2.put("version", account.getVersion());
            String repsingstr=SignTools.getSignStr(m2);

            String repsign= MD5.MD5Encode(md5key+repsingstr);
            log.info("返回签名明文："+repsingstr);
            log.info("返回签名密文："+repsign);
            transferFundVO.setSignMsg(repsign);
            log.info("************** 转账服务 CSTransferAccount *************  转账成功，时间：{}",new Date());
        }else{
            //转账失败
            Map<String, String> m2=new HashMap<String, String>();
            transferFundVO.setRespCode(Const.CSCode.CODE_CS0009);
            transferFundVO.setRespMsg(Const.CSCode.MSG_CS0009);
            m2.put("respCode", Const.CSCode.CODE_CS0009);
            m2.put("respMsg", Const.CSCode.MSG_CS0009);
            m2.put("version", account.getVersion());
            String repsingstr= SignTools.getSignStr(m2);
            String repsign= MD5.MD5Encode(md5key+repsingstr);
            log.info("返回签名明文："+repsingstr);
            log.info("返回签名密文："+repsign);
            account.setSignMsg(repsign);
            log.info("数据处理异常");
            log.info("************** 转账服务 CSTransferAccount ************* :{}，时间：{}",message2.getMsg(),new Date());
        }
        log.info("************** 转账服务 CSTransferAccount ************* # 结束，时间：{}" , new Date());
        return transferFundVO;
    }

}
