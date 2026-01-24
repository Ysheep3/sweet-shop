package com.sweet.user.service;

import com.alipay.api.domain.AddressDTO;
import com.sweet.user.entity.dto.AddressAddDTO;
import com.sweet.user.entity.vo.AddressVO;

import java.util.List;

public interface AddressService {
    List<AddressVO> list();

    AddressVO getById(Long id);

    void create(AddressAddDTO requestParam);

    void update(AddressAddDTO requestParam);

    void delete(Long id);

    AddressVO getDefaultAddress();
}
