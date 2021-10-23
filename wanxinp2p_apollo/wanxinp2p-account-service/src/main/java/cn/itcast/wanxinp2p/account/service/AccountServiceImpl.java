package cn.itcast.wanxinp2p.account.service;

import cn.itcast.wanxinp2p.account.common.AccountErrorCode;
import cn.itcast.wanxinp2p.account.entity.Account;
import cn.itcast.wanxinp2p.account.mapper.AccountMapper;
import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountLoginDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.BusinessException;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.common.util.PasswordUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    //注入与验证码相关的功能的对象SmsService
    @Autowired
    private SmsService smsService;
    //配置文件中设置的开关
    @Value("${sms.enable}")
    private Boolean smsEnable;



    /**
     * 获取手机验证码
     *
     * @param mobile 手机号
     * @return 含有验证码等信息的RestResponse对象
     */
    @Override
    public RestResponse getSMSCode(String mobile) {
        return smsService.getSmsCode(mobile);
    }

    /**
     * 校验手机号和验证码
     *
     * @param mobile 手机号
     * @param key    校验标识
     * @param code   验证码
     * @return
     */
    @Override
    public Integer checkMobile(String mobile, String key, String code) {
        //校验验证码 失败抛出E_140152(140152,"验证码错误"）
        smsService.verifySmsCode(key, code);
        //判断手机号是否存在
        //条件构造器
        QueryWrapper<Account> wrapper = new QueryWrapper<>();
        //封装条件
        //wrapper.eq("mobile", mobile);
        //使用lambda表达式方法封装条件
        wrapper.lambda().eq(Account::getMobile,mobile);
        //调用count方法查询手机号存在数量 >0 存在相同手机号 已注册
        int count = count(wrapper);
        return count > 0 ? 1 : 0;
    }

    /**
     * 用户注册
     * @param accountRegisterDTO 封装用户注册信息
     * @return 用户及权限信息
     */
    @Override
    @Hmily(confirmMethod = "confirmRegister",cancelMethod = "cancelRegister")
    public AccountDTO register(AccountRegisterDTO accountRegisterDTO) {
        Account account = new Account();
        account.setUsername(accountRegisterDTO.getUsername());
        account.setMobile(accountRegisterDTO.getMobile());
        account.setPassword(PasswordUtil.generate(accountRegisterDTO.getPassword()));
        //默认手机号为密码
        if (smsEnable){
            account.setPassword(PasswordUtil.generate(accountRegisterDTO.getMobile()));
        }
        account.setDomain("c");
        //save保存
        save(account);
        //entity转为DTO
        return convertAccountEntityToDTO(account);
    }
    public void confirmRegister(AccountRegisterDTO accountRegisterDTO){
        log.info("execute confirmRegister");
    }
    public void cancelRegister(AccountRegisterDTO accountRegisterDTO){
        log.info("execute confirmRegister");
        //删除账号
        remove(Wrappers.<Account>lambdaQuery().eq(Account::getUsername,accountRegisterDTO.getUsername()));
    }

    /**
     * 用户登录
     * @param accountLoginDTO 封装用户登录信息
     * @return 用户及权限信息
     */
    @Override
    public AccountDTO login(AccountLoginDTO accountLoginDTO) {
        Account account = null;
        //判断域(c：c端用户；b：b端用户)
        if (accountLoginDTO.getDomain().equalsIgnoreCase("c")){
            //获取c端用户
            account = getAccountByMobile(accountLoginDTO.getMobile());
        } else {
            //获取b端用户
            account = getAccountByUsername(accountLoginDTO.getUsername());
        }
        //为空则没查询到对象，说明该用户未注册，抛出异常
        if (account == null){
            throw new BusinessException(AccountErrorCode.E_130104);
        }
        //entity转为dto
        AccountDTO accountDTO = convertAccountEntityToDTO(account);
        if (smsEnable){// 如果smsEnable=true，说明是短信验证码登录，不做密码校验
            return accountDTO;
        }   //验证密码
        if (PasswordUtil.verify(accountLoginDTO.getPassword(),account.getPassword())){
            return accountDTO;
        }
        throw new BusinessException(AccountErrorCode.E_130105);
    }


    /**
     * entity转为dto
     * @param entity
     * @return
     */
    private AccountDTO convertAccountEntityToDTO(Account entity) {
        if (entity == null) {
            return null;
        }
        AccountDTO dto = new AccountDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     根据手机获取账户信息
     @param mobile 手机号
     @return 账户实体
     */
    private Account getAccountByMobile(String mobile){
        return getOne(new QueryWrapper<Account>().lambda().eq(Account::getMobile, mobile));
    }

    /**
     根据用户名获取账户信息
     @param username 用户名
     @return 账户实体
     */
    private Account getAccountByUsername(String username){
        return getOne(new QueryWrapper<Account>().lambda().eq(Account::getUsername, username));
    }


}
