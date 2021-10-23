package cn.itcast.wanxinp2p.consumer.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRegisterDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.consumer.entity.Consumer;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ConsumerService extends IService<Consumer> {
    /**
     * 检查用户名是否存在
     * @param mobile 手机号
     * @return 0 不存在 1 存在
     */
    Integer checkMobile(String mobile);

    /**
     * 用户注册 保存用户注册信息
     * @param consumerRegisterDTO 用户注册信息
     */
    void register(ConsumerRegisterDTO consumerRegisterDTO);

    /**
     生成开户数据
     @param consumerRequest 平台c端用户开户信息
     @return 与银行存管系统对接使用的签名请求数据
     */
    RestResponse<GatewayRequest> createConsumer(ConsumerRequest consumerRequest);

}
