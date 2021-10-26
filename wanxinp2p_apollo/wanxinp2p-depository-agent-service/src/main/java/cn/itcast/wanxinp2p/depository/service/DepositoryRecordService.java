package cn.itcast.wanxinp2p.depository.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.depository.entity.DepositoryRecord;
import com.baomidou.mybatisplus.extension.service.IService;

public interface DepositoryRecordService extends IService<DepositoryRecord> {

    /**
     * 开通存管账户
     * @param consumerRequest 开户信息
     * @return 与银行存管系统对接使用的签名请求数据
     */
    GatewayRequest createConsumer(ConsumerRequest consumerRequest);

    /**
     * 根据请求流水号更新请求状态
     * @param requestNo 请求流水号
     * @param requestsStatus 请求状态  0未同步 1已同步 2银行存管系统处理失败
     * @return 修改是否成功
     */
    Boolean modifyRequestStatus(String requestNo, Integer requestsStatus);
}
