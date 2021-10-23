package cn.itcast.wanxinp2p.account.controller;

import cn.itcast.wanxinp2p.account.service.AccountService;
import cn.itcast.wanxinp2p.api.account.AccountAPI;
import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountLoginDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(value = "统一账户服务", tags = "Account", description = "同一账号服务API")
public class AccountController implements AccountAPI {

    @Autowired
    private AccountService accountService;

    @Override
    @ApiOperation("获取手机验证码")
    @ApiImplicitParam(name = "mobile", value = "手机号", dataType = "String")
    @GetMapping("/sms/{mobile}")
    public RestResponse getSMSCode(@PathVariable String mobile) {
        return accountService.getSMSCode(mobile);
    }


    @Override
    @ApiOperation("验证手机号和验证码")
    @ApiImplicitParams({@ApiImplicitParam(name = "mobile", value = "手机号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "key", value = "校验标识", required = true, dataType = "String"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, dataType = "String")})
    @GetMapping(value = "/mobiles/{mobile}/key/{key}/code/{code}")
    public RestResponse<Integer> checkMobile(@PathVariable String mobile,
                                             @PathVariable String key,
                                             @PathVariable String code) {
        return RestResponse.success(accountService.checkMobile(mobile,key,code));
    }



    @Override
    @ApiOperation("用户注册")
    @ApiImplicitParam(name = "accountRegisterDTO", value = "账户注册信息",
            required = true, dataType = "AccountRegisterDTO", paramType = "body")
    @PostMapping(value = "/l/accounts")      //不暴露给前端地址以l开头
    public RestResponse<AccountDTO> register(@RequestBody AccountRegisterDTO accountRegisterDTO) {
        return RestResponse.success(accountService.register(accountRegisterDTO));
    }


    @Override
    @ApiOperation("用户登录")
    @ApiImplicitParam(name = "accountLoginDTO", value = "用户登录信息",
            required = true, dataType = "AccountLoginDTO", paramType = "body")
    @PostMapping(value = "/l/accounts/session")
    public RestResponse<AccountDTO> login(@RequestBody AccountLoginDTO accountLoginDTO) {
        return RestResponse.success(accountService.login(accountLoginDTO));
    }
}
