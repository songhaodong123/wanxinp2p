package cn.itcast.wanxinp2p.consumer.message;

import cn.itcast.wanxinp2p.api.depository.model.DepositoryConsumerResponse;
import cn.itcast.wanxinp2p.consumer.service.ConsumerService;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/*
    从MQ中接收消息
 */
@Component
public class GatewayNotifyConsumer {

    @Autowired
    private ConsumerService consumerService;

//    @Value("${rocketmq.consumer.group}")
//    private String consumerGroup;
//
//    @Value("${rocketmq.name-server}")
//    private String nameServer;

    public GatewayNotifyConsumer(@Value("${rocketmq.consumer.group}") String consumerGroup,@Value("${rocketmq.name-server}") String nameServer) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer(consumerGroup);
        defaultMQPushConsumer.setNamesrvAddr(nameServer);
        defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere
                .CONSUME_FROM_LAST_OFFSET);
        defaultMQPushConsumer.subscribe("TP_GATEWAY_NOTIFY_AGENT", "*");
        //注册监听器
        defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                try {
                    Message message = msgs.get(0);
                    String topic = message.getTopic();
                    String tag = message.getTags();
                    String body = new String(message.getBody(),
                            StandardCharsets.UTF_8);
                    if (tag.equals("PERSONAL_REGISTER")) {
                        DepositoryConsumerResponse response =
                                JSON.parseObject(body,
                                        DepositoryConsumerResponse.class);
                        consumerService.modifyResult(response);
                    }
                //if...
                } catch (Exception e) {
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        defaultMQPushConsumer.start();
    }
}
