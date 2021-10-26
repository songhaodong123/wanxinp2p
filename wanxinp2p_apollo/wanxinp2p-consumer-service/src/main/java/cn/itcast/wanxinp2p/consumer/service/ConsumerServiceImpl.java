package cn.itcast.wanxinp2p.consumer.service;

import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.api.consumer.model.BankCardDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRegisterDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.DepositoryConsumerResponse;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.common.domain.*;
import cn.itcast.wanxinp2p.common.util.CodeNoUtil;
import cn.itcast.wanxinp2p.consumer.agent.AccountApiAgent;
import cn.itcast.wanxinp2p.consumer.agent.DepositoryAgentApiAgent;
import cn.itcast.wanxinp2p.consumer.common.ConsumerErrorCode;
import cn.itcast.wanxinp2p.consumer.entity.BankCard;
import cn.itcast.wanxinp2p.consumer.entity.Consumer;
import cn.itcast.wanxinp2p.consumer.mapper.ConsumerMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ConsumerServiceImpl extends ServiceImpl<ConsumerMapper, Consumer> implements ConsumerService{

    //注入框架产生的代理对象
    @Autowired
    private AccountApiAgent accountApiAgent;
    @Autowired
    private DepositoryAgentApiAgent depositoryAgentApiAgent;
    //注入银行卡表的相关行为
    @Autowired
    private BankCardService bankCardService;

    /**
     * 根据手机号获取用户信息
     * @param mobile 手机号
     * @return
     */
    private ConsumerDTO getByMobile(String mobile){
        Consumer consumer = getOne(new QueryWrapper<Consumer>().lambda().eq(Consumer::getMobile, mobile));
        return convertConsumerEntityToDTO(consumer);
    }

    /**
     * entity转为dto
     * @param entity
     * @return
     */
    private ConsumerDTO convertConsumerEntityToDTO(Consumer entity) {
        if (entity == null) {
            return null;
        }
        ConsumerDTO dto = new ConsumerDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }


    /**
     *  判断手机号是否存在
     * @param mobile 手机号
     * @return
     */
    @Override
    public Integer checkMobile(String mobile) {
        return getByMobile(mobile) != null ? 1 : 0;
    }



    /**
     * 用户注册 保存用户注册信息
     * @param consumerRegisterDTO 用户注册信息
     */
    @Override
    @Hmily(confirmMethod = "confirmRegister", cancelMethod = "cancelRegister")
    public void register(ConsumerRegisterDTO consumerRegisterDTO) {
        //检测是否已经注册
        if (checkMobile(consumerRegisterDTO.getMobile()) == 1){
            //存在抛出自定义异常 用户已存在
            throw new BusinessException(ConsumerErrorCode.E_140107);
        }
        Consumer consumer = new Consumer();
        BeanUtils.copyProperties(consumerRegisterDTO, consumer);
        //设置用户名
        /*
            设置标的编码, 请求流水号等业务标识
            可根据业务进行增加
            CodeNoUtil 标的编码, 请求流水号生成工具类
            CodePrefixCode 枚举类 没有标的 不使前缀
         */
        consumer.setUsername(CodeNoUtil.getNo(CodePrefixCode.CODE_NO_PREFIX));
        /*
        Errors
            HideResolver error at paths./consumers.post.parameters.0.schema.$ref
            Could not resolve reference because of: Could not resolve pointer: /definitions/AccountRegisterDTO does not exist in document
         */
        consumerRegisterDTO.setUsername(consumer.getUsername());
        //设置UserNo 用户编码,生成唯一,用户在存管系统标识  请求流水号前缀
        consumer.setUserNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
        //是否已绑定银行卡
        consumer.setIsBindCard(0);
        //保存用户信息
        save(consumer);

        //发起远程调用到Account
        AccountRegisterDTO accountRegisterDTO = new AccountRegisterDTO();
        //将consumerRegisterDTO 转为 accountRegisterDTO
        BeanUtils.copyProperties(consumerRegisterDTO, accountRegisterDTO);
        RestResponse<AccountDTO> restResponse = accountApiAgent.register(accountRegisterDTO);
        //如果没有成功 抛出注册失败异常
        if (restResponse.getCode() != CommonErrorCode.SUCCESS.getCode()){
            throw new BusinessException(ConsumerErrorCode.E_140106);
        }
    }
    public void confirmRegister(ConsumerRegisterDTO consumerRegisterDTO){
        log.info("execute confirmRegister");
    }
    public void cancelRegister(ConsumerRegisterDTO consumerRegisterDTO){
        log.info("execute cancelRegister");
        //将保存的数据删除
        remove(Wrappers.<Consumer>lambdaQuery().eq(Consumer::getMobile,consumerRegisterDTO.getMobile()));
    }

    /**
     生成开户数据
     @param consumerRequest 平台c端用户开户信息
     @return 与银行存管系统对接使用的签名请求数据
     */
    @Override
    //事务管理
    @Transactional
    public RestResponse<GatewayRequest> createConsumer(ConsumerRequest consumerRequest) {
        ConsumerDTO consumerDTO = getByMobile(consumerRequest.getMobile());
        //判断银行卡是否已经开户
        if (consumerDTO.getIsBindCard() == 1){
            throw new BusinessException(ConsumerErrorCode.E_140105);
        }
        //判断银行卡是否已被绑定
        BankCardDTO bankCardDTO = bankCardService.getByCardNumber(consumerRequest.getCardNumber());
        if (bankCardDTO != null && bankCardDTO.getStatus()== StatusCode.STATUS_IN.getCode()){
            throw new BusinessException(ConsumerErrorCode.E_140151);
        }

        //更新用户开户信息 创建时的id
        consumerRequest.setId(consumerDTO.getId());
        //产生请求流水号和用户编码前缀
        consumerRequest.setRequestNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
        consumerRequest.setUserNo(CodeNoUtil.getNo(CodePrefixCode.CODE_CONSUMER_PREFIX));
        //设置查询条件和需要更新的数据
//        UpdateWrapper<Consumer> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.lambda().eq(Consumer::getMobile, consumerDTO.getMobile());
//        updateWrapper.lambda().set(Consumer::getUserNo, consumerRequest.getUserNo());
//        updateWrapper.lambda().set(Consumer::getRequestNo, consumerRequest.getRequestNo());
//        updateWrapper.lambda().set(Consumer::getFullname, consumerRequest.getFullname());
//        updateWrapper.lambda().set(Consumer::getIdNumber, consumerRequest.getIdNumber());
//        updateWrapper.lambda().set(Consumer::getAuthList, "ALL");
//        update(updateWrapper);
        update(Wrappers.<Consumer>lambdaUpdate()
                .eq(Consumer::getMobile, consumerDTO.getMobile())
                .set(Consumer::getUserNo, consumerRequest.getUserNo())
                .set(Consumer::getRequestNo, consumerRequest.getRequestNo())
                .set(Consumer::getFullname, consumerRequest.getFullname())
                .set(Consumer::getIdNumber, consumerRequest.getIdNumber())
                .set(Consumer::getAuthList, "ALL"));

        //保存用户绑卡信息
        BankCard bankCard = new BankCard();
        bankCard.setConsumerId(consumerDTO.getId());
        bankCard.setBankCode(consumerRequest.getBankCode());
        bankCard.setCardNumber(consumerRequest.getCardNumber());
        bankCard.setMobile(consumerRequest.getMobile());
        bankCard.setStatus(StatusCode.STATUS_OUT.getCode());
        BankCardDTO existBankCard = bankCardService.getByConsumerId(bankCard.getConsumerId());
        if (existBankCard != null) {
            bankCard.setId(existBankCard.getId());
        }
        bankCardService.saveOrUpdate(bankCard);
        return depositoryAgentApiAgent.createConsumer(consumerRequest);
    }

    /**
     * 更新开户结果
     * @param response
     * @return
     */
    @Override
    @Transactional
    public Boolean modifyResult(DepositoryConsumerResponse response) {
        //1.获取数据（状态）
        int status = response.getRespCode().equals(DepositoryReturnCode.RETURN_CODE_00000.getCode())
                ? StatusCode.STATUS_IN.getCode() : StatusCode.STATUS_FAIL.getCode();
        //2.更新开户结果用户信息
        Consumer consumer = getByRequestNo(response.getRequestNo());
        update(Wrappers.<Consumer>lambdaUpdate()
                .eq(Consumer::getId, consumer.getId())
                .set(Consumer::getIsBindCard, status)
                .set(Consumer::getStatus, status));
        //3.更新银行卡信息
        return bankCardService.update(Wrappers.<BankCard>lambdaUpdate()
                .eq(BankCard::getConsumerId, consumer.getId())
                .set(BankCard::getStatus, status).set(BankCard::getBankCode, response.getBankCode())
                .set(BankCard::getBankName, response.getBankName()));
    }

    /**
     * 根据DepositoryConsumerResponse中的requestNo 获取Consumer对象
     * @param requestNo DepositoryConsumerResponse的流水号
     * @return Consumer
     */
    private Consumer getByRequestNo(String requestNo){
        return getOne(Wrappers.
                <Consumer>lambdaQuery().eq(Consumer::getRequestNo,requestNo));
    }
}
