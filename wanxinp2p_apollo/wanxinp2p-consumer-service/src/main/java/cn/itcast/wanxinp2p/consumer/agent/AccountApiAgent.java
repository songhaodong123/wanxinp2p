package cn.itcast.wanxinp2p.consumer.agent;

import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//统一账户的代理
//远程调用统一账户中心微服务
@FeignClient(value = "account-service")  //设置要访问的微服务
public interface AccountApiAgent {

    /**
     * 用户注册 保存信息
     * @param accountRegisterDTO 用户注册信息
     * @return 封装注册信息
     */
    @PostMapping(value = "/account/l/accounts")
    @Hmily
    RestResponse<AccountDTO> register(@RequestBody AccountRegisterDTO accountRegisterDTO);

}
