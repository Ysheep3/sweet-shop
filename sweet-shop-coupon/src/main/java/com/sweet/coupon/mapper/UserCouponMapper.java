package com.sweet.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sweet.coupon.entity.dto.UserCouponDTO;
import com.sweet.coupon.entity.pojo.Coupon;
import com.sweet.coupon.entity.pojo.UserCoupon;
import com.sweet.coupon.entity.vo.UserCouponVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    List<UserCouponDTO> list(Long userId, Integer status);
}
