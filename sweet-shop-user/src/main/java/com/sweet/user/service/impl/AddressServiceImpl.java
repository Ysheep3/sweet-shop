package com.sweet.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alipay.api.domain.AddressDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.AddressBookBusinessException;
import com.sweet.user.entity.dto.AddressAddDTO;
import com.sweet.user.entity.pojo.Address;
import com.sweet.user.entity.vo.AddressVO;
import com.sweet.user.mapper.AddressMapper;
import com.sweet.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressMapper addressMapper;

    @Override
    public List<AddressVO> list() {
        LambdaQueryWrapper<Address> wrapper = Wrappers.lambdaQuery(Address.class)
                .eq(Address::getUserId, BaseContext.getCurrentId());
        List<Address> addresses = addressMapper.selectList(wrapper);

        if (CollUtil.isEmpty(addresses)) {
            return List.of();
        }

        List<AddressVO> addressVOS = new ArrayList<>();
        for (Address address : addresses) {
            AddressVO addressVO = BeanUtil.toBean(address, AddressVO.class);
            addressVOS.add(addressVO);
        }

        return addressVOS;
    }

    @Override
    public AddressVO getById(Long id) {
        if (id == null) {
            throw new AddressBookBusinessException(MessageConstant.GET_ERROR);
        }

        LambdaQueryWrapper<Address> wrapper = Wrappers.lambdaQuery(Address.class)
                .eq(Address::getId, id)
                .eq(Address::getUserId, BaseContext.getCurrentId());

        Address address = addressMapper.selectOne(wrapper);
        if (address == null) {
            throw new AddressBookBusinessException(MessageConstant.GET_ERROR);
        }

        return BeanUtil.toBean(address, AddressVO.class);
    }

    @Override
    public void create(AddressAddDTO requestParam) {
        if (requestParam == null) {
            throw new AddressBookBusinessException(MessageConstant.DO_ERROR);
        }

        Address address = BeanUtil.toBean(requestParam, Address.class);
        address.setUserId(BaseContext.getCurrentId());

        int row = addressMapper.insert(address);
        if (row < 1) {
            throw new AddressBookBusinessException(MessageConstant.INSERT_ERROR);
        }
    }

    @Override
    public void update(AddressAddDTO requestParam) {
        if (requestParam == null) {
            throw new AddressBookBusinessException(MessageConstant.DO_ERROR);
        }

        Address address = BeanUtil.toBean(requestParam, Address.class);

        if (requestParam.getIsDefault() == 1) {
            // 将其他地址设为非默认地址
            Address updateAddress = new Address();
            updateAddress.setIsDefault(0);

            addressMapper.update(updateAddress,
                    Wrappers.lambdaQuery(Address.class)
                            .eq(Address::getUserId, BaseContext.getCurrentId())
                            .eq(Address::getIsDefault, 1));
        }

        int row = addressMapper.update(address,
                Wrappers.lambdaQuery(Address.class)
                        .eq(Address::getUserId, BaseContext.getCurrentId())
                        .eq(Address::getId, address.getId()));

        if (row < 1) {
            throw new AddressBookBusinessException(MessageConstant.UPDATE_ERROR);
        }

    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new AddressBookBusinessException(MessageConstant.DO_ERROR);
        }

        int row = addressMapper.deleteById(id);

        if (row < 1) {
            throw new AddressBookBusinessException(MessageConstant.DELETE_ERROR);
        }
    }
}
