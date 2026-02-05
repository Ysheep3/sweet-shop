package com.sweet.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sweet.coupon.entity.dto.UserCouponDTO;
import com.sweet.coupon.entity.pojo.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    List<UserCouponDTO> list(Long userId, Integer status);

        /**
         * 仅查询指定 id 集合中的用户券，仍然按状态和时间做过滤
         */
        List<UserCouponDTO> listByIds(@Param("userId") Long userId,
                                      @Param("status") Integer status,
                                      @Param("ids") List<Long> ids);
}
