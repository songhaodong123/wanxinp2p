package cn.itcast.wanxinp2p.account.service;

import cn.itcast.wanxinp2p.account.common.AccountErrorCode;
import cn.itcast.wanxinp2p.common.domain.BusinessException;
import cn.itcast.wanxinp2p.common.domain.CommonErrorCode;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.common.util.OkHttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/*
    与验证码有关的功能
 */
@Service
public class SmsService {

    //配置文件中设置的验证码服务的url地址
    @Value("${sms.url}")
    private String smsURL;

    //配置文件中设置的开关
    @Value("${sms.enable}")
    private Boolean smsEnable;

    /**
     * 调用验证码服务发送验证码
     * @param mobile 手机号
     * @return
     */
    public RestResponse getSmsCode(String mobile) {
        //通过配置文件的配置，判断是否开启验证码服务,没开启则以手机号为验证码
        if (smsEnable){
            return OkHttpUtil.post(smsURL + "/generate?effectiveTime=300&name=sms", "{\"mobile\":"+mobile+"}");
        }
        return RestResponse.success();
    }

    /**
     * 校验验证码
     * @param key 校验标识 redis中的键
     * @param code 短信验证码
     */
    public void verifySmsCode(String key, String code){
        //通过配置文件的配置，判断是否开启验证码服务,没开启则以手机号为验证码
        if (smsEnable) {
            //设置校验验证码服务的url
            //http://localhost:56085/sailing/verify?name=sms "&verificationKey= &verificationCode= "
            StringBuilder params = new StringBuilder("/verify?name=sms");
            params.append("&verificationKey=").append(key);
            params.append("&verificationCode=").append(code);
            RestResponse smsResponse = OkHttpUtil.post(smsURL + params, "");
            //将校验后的数据判断验证码是否正确
            //判断：Code的值是否为0(SUCCESS(0, "成功")  判断响应内容是否为false
            //失败 抛出自定义异常 E_140152(140152,"验证码错误")
            if (smsResponse.getCode() != CommonErrorCode.SUCCESS.getCode() ||
                    smsResponse.getResult().toString().equalsIgnoreCase("false")) {
                throw new BusinessException(AccountErrorCode.E_140152);
            }

        }
    }

}
