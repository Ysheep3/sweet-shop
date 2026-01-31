package com.sweet.coupon.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.CouponBusinessException;
import com.sweet.coupon.common.UserCouponStatusEnum;
import com.sweet.coupon.entity.dto.UserCouponClaimDTO;
import com.sweet.coupon.entity.dto.UserCouponDTO;
import com.sweet.coupon.entity.pojo.Coupon;
import com.sweet.coupon.entity.pojo.UserCoupon;
import com.sweet.coupon.entity.vo.UserCouponVO;
import com.sweet.coupon.mapper.CouponMapper;
import com.sweet.coupon.mapper.UserCouponMapper;
import com.sweet.coupon.mq.event.UserCouponExecuteStatusEvent;
import com.sweet.coupon.mq.producer.UserCouponDelayExecuteEndProducer;
import com.sweet.coupon.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl implements UserCouponService {

    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;
    private final UserCouponDelayExecuteEndProducer endProducer;

    @Override
    public List<UserCouponVO> listByStatus(Integer status) {
        List<UserCouponDTO> userCouponDTOS = userCouponMapper.list(BaseContext.getCurrentId(), status);
        if (CollUtil.isEmpty(userCouponDTOS)) {
            return List.of();
        }

        List<UserCouponVO> userCouponVOS = new ArrayList<>();
        for (UserCouponDTO userCouponDTO : userCouponDTOS) {
            List<String> tags = StrUtil.splitTrim(userCouponDTO.getTags(), ",");
            UserCouponVO userCouponVO = BeanUtil.copyProperties(userCouponDTO, UserCouponVO.class);
            userCouponVO.setTags(tags);
            userCouponVOS.add(userCouponVO);
        }

        return userCouponVOS;
    }

    @Override
    public void claimCoupon(UserCouponClaimDTO requestParam) {
        Long couponId = requestParam.getCouponId();
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new CouponBusinessException(MessageConstant.GET_COUPON_ERROR);
        }

        List<UserCoupon> userCoupons = userCouponMapper.selectList(
                Wrappers.lambdaQuery(UserCoupon.class)
                        .eq(UserCoupon::getUserId, BaseContext.getCurrentId())
                        .eq(UserCoupon::getCouponId, couponId)
        );

        int receiveCount;
        if (CollUtil.isNotEmpty(userCoupons)) {
            // 判断是否超过用户领取限制
            receiveCount = userCoupons.stream()
                    .map(UserCoupon::getReceiveCount)
                    .reduce(0, Integer::sum);

            if (receiveCount >= coupon.getLimitPerUser()) {
                throw new CouponBusinessException(MessageConstant.CLAIM_COUPON_LIMIT_ERROR);
            }

            receiveCount = receiveCount + 1;
            UserCoupon userCouponUpd = userCoupons.get(userCoupons.size() - 1);
            userCouponUpd.setReceiveCount(receiveCount);

            if (userCouponUpd.getStatus().equals(UserCouponStatusEnum.UNUSED.getCode())) {
                userCouponMapper.updateById(userCouponUpd);
            } else {
                // 如果最后一张优惠券状态不是未使用，则新增一张优惠券记录
                userCouponUpd = BeanUtil.toBean(requestParam, UserCoupon.class);
                userCouponUpd.setStatus(UserCouponStatusEnum.UNUSED.getCode());
                userCouponUpd.setUserId(BaseContext.getCurrentId());
                userCouponUpd.setReceiveTime(LocalDateTime.now());
                userCouponUpd.setReceiveCount(1);
                userCouponMapper.insert(userCouponUpd);

            }
            // 延迟队列设置过期
            sendDelayMessage(userCouponUpd);

            return;
        }

        UserCoupon userCoupon = BeanUtil.toBean(requestParam, UserCoupon.class);
        userCoupon.setStatus(UserCouponStatusEnum.UNUSED.getCode());
        userCoupon.setUserId(BaseContext.getCurrentId());
        userCoupon.setReceiveTime(LocalDateTime.now());
        userCoupon.setReceiveCount(1);

        userCouponMapper.insert(userCoupon);

        // 延迟队列设置过期

        sendDelayMessage(userCoupon);
    }

    private void sendDelayMessage(UserCoupon userCoupon) {
        LocalDateTime endTime;
        endTime = userCoupon.getEndTime();
        long endTimeMills = endTime.atZone(ZoneId.of("Asia/Shanghai"))
                .toInstant()
                .toEpochMilli();

        UserCouponExecuteStatusEvent event = UserCouponExecuteStatusEvent.builder()
                .userCouponId(userCoupon.getId())
                .userId(BaseContext.getCurrentId())
                .couponId(userCoupon.getCouponId())
                .deliverTime(endTimeMills)
                .build();

        endProducer.sendMessage(event);
    }

    @Override
    public void useCoupon(Long id) {
        if (id == null) {
            throw new CouponBusinessException(MessageConstant.DO_ERROR);
        }

        UserCoupon userCoupon = userCouponMapper.selectOne(
                Wrappers.lambdaQuery(UserCoupon.class)
                        .eq(UserCoupon::getId, id)
                        .eq(UserCoupon::getUserId, BaseContext.getCurrentId())
        );
        if (userCoupon == null) {
            throw new CouponBusinessException(MessageConstant.GET_COUPON_ERROR);
        }
        userCoupon.setStatus(UserCouponStatusEnum.USED.getCode());
        userCoupon.setUseTime(LocalDateTime.now());

        userCouponMapper.updateById(userCoupon);
    }
}
