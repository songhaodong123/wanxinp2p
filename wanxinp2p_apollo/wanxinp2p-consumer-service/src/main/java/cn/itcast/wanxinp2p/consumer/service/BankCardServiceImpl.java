package cn.itcast.wanxinp2p.consumer.service;

import cn.itcast.wanxinp2p.api.consumer.model.BankCardDTO;
import cn.itcast.wanxinp2p.consumer.entity.BankCard;
import cn.itcast.wanxinp2p.consumer.mapper.BankCardMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class BankCardServiceImpl extends ServiceImpl<BankCardMapper, BankCard> implements BankCardService{

    /**
     * 获取银行卡信息
     * @param consumerId 用户ID
     * @return 银行卡信息
     */
    @Override
    public BankCardDTO getByConsumerId(Long consumerId) {
        BankCard bankCard = getOne(new QueryWrapper<BankCard>().lambda().eq(BankCard::getConsumerId, consumerId));
        return convertBankCardEntityToDTO(bankCard);
    }

    /**
     * 获取银行卡信息
     * @param cardNumber 卡号
     * @return 银行卡信息
     */
    @Override
    public BankCardDTO getByCardNumber(String cardNumber) {
        BankCard bankCard = getOne(new QueryWrapper<BankCard>().lambda().eq(BankCard::getCardNumber, cardNumber));
        return convertBankCardEntityToDTO(bankCard);
    }

    /**
     * entity转为dto
     * @param entity entity
     * @return dto
     */
    private BankCardDTO convertBankCardEntityToDTO(BankCard entity) {
        if (entity == null) {
            return null;
        }
        BankCardDTO dto = new BankCardDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
