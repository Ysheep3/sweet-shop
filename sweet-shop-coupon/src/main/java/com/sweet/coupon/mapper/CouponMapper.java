package com.sweet.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sweet.coupon.entity.dto.CouponPageDTO;
import com.sweet.coupon.entity.pojo.Coupon;
import com.sweet.coupon.entity.vo.AdminCouponVO;
import com.sweet.coupon.entity.vo.CouponVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    IPage<AdminCouponVO> pageQuery(Page<AdminCouponVO> page, @Param("coupon") CouponPageDTO couponPageDTO);
}
