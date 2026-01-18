package com.sweet.user.controller;

import com.alipay.api.domain.AddressDTO;
import com.sweet.common.result.Result;
import com.sweet.user.entity.dto.AddressAddDTO;
import com.sweet.user.entity.vo.AddressVO;
import com.sweet.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("addressController")
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @GetMapping("/list")
    public Result<List<AddressVO>> list() {
        List<AddressVO> addressVOS = addressService.list();
        return Result.success(addressVOS);
    }

    @GetMapping("/{id}")
    public Result<AddressVO> getById(@PathVariable Long id) {
        AddressVO addressVO = addressService.getById(id);
        return Result.success(addressVO);
    }

    @PostMapping
    public Result<Void> createAddress(@RequestBody AddressAddDTO requestParam) {
        addressService.create(requestParam);
        return Result.success();
    }

    @PutMapping
    public Result<Void> updateAddress(@RequestBody AddressAddDTO requestParam) {
        addressService.update(requestParam);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        addressService.delete(id);
        return Result.success();
    }
}
