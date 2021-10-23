package cn.itcast.wanxinp2p.api.consumer;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRegisterDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.common.domain.RestResponse;

public interface ConsumerAPI {
    /**
     * 用户注册 保存用户信息
     * @param consumerRegisterDTO 用户注册信息
     * @return true或false
     */
    RestResponse register(ConsumerRegisterDTO consumerRegisterDTO);

    /**
     * 生成开户请求数据
     * @param consumerRequest 开户信息
     * @return
     */
    RestResponse<GatewayRequest> createConsumer(ConsumerRequest consumerRequest);
}
