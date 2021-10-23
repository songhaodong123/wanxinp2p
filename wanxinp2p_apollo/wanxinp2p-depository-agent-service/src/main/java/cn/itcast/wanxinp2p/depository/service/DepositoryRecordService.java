package cn.itcast.wanxinp2p.depository.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.depository.entity.DepositoryRecord;
import com.baomidou.mybatisplus.extension.service.IService;

public interface DepositoryRecordService extends IService<DepositoryRecord> {

    /**
     * 开通存管账户
     * @param consumerRequest 开户信息
     * @return
     */
    GatewayRequest createConsumer(ConsumerRequest consumerRequest);
}
