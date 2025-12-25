package com.sweet.user.service.impl;

import com.sweet.user.mapper.AddressMapper;
import com.sweet.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressMapper addressMapper;
}
