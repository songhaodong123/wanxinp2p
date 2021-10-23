package cn.itcast.wanxinp2p.consumer.controller;

import cn.itcast.wanxinp2p.api.consumer.ConsumerAPI;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRegisterDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.common.util.EncryptUtil;
import cn.itcast.wanxinp2p.consumer.common.util.SecurityUtil;
import cn.itcast.wanxinp2p.consumer.service.ConsumerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "用户服务的Controller", tags = "Consumer", description = "用户服务API") //swagger的注解：用于修饰整个类，描述Controller的作用
public class ConsumerController implements ConsumerAPI {

    @Autowired
    private ConsumerService consumerService;

    /*
     * 登录注册相关
     */
    @Override
    @PostMapping(value = "/consumers")
    @ApiOperation("用户注册")
    @ApiImplicitParam(name = "consumerRegisterDTO", value = "注册信息",
            required = true, dataType = "ConsumerRegisterDTO", paramType = "body")
    public RestResponse register(@RequestBody ConsumerRegisterDTO consumerRegisterDTO) {
        consumerService.register(consumerRegisterDTO);
        return RestResponse.success();
    }
    @ApiOperation("过网关受保护资源，进行认证拦截测试")
    @ApiImplicitParam(name = "jsonToken", value = "访问令牌", required = true, dataType = "String")
    @GetMapping(value = "/m/consumers/test")
    public RestResponse<String> testResources(String jsonToken) {
        //解码
        return RestResponse.success(EncryptUtil.decodeUTF8StringBase64(jsonToken));
    }


    /*
     * 开户相关
     */
    @Override
    @PostMapping(value = "/my/consumers")
    @ApiOperation("生成开户请求数据")
    @ApiImplicitParam(name = "consumerRequest", value = "开户信息", required = true,
                dataType = "ConsumerRequest", paramType = "body")
    public RestResponse<GatewayRequest> createConsumer(@RequestBody ConsumerRequest consumerRequest) {
        //SecurityUtil：从jsonToken获取当前登录用户
        consumerRequest.setMobile(SecurityUtil.getUser().getMobile());
        return consumerService.createConsumer(consumerRequest);
    }
}
