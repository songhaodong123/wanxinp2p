package cn.itcast.wanxinp2p.account.service;

import cn.itcast.wanxinp2p.account.entity.Account;
import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountLoginDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AccountService extends IService<Account> {
    /**
     * 获取手机验证码
     * @param mobile 手机号
     * @return 验证码
     */
    RestResponse getSMSCode(String mobile);

    /**
     * 校验手机号和验证码
     * @param mobile 手机号
     * @param key 校验标识
     * @param code 验证码
     * @return
     */
    Integer checkMobile(String mobile, String key, String code);

    /**
     * 用户注册
     * @param accountRegisterDTO 封装用户注册信息
     * @return 用户及权限信息
     */
    AccountDTO register(AccountRegisterDTO accountRegisterDTO);

    /**
     * 用户登录
     * @param accountLoginDTO 封装用户登录信息
     * @return 用户及权限信息
     */
    AccountDTO login(AccountLoginDTO accountLoginDTO);
}
