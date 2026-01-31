package com.sweet.coupon.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.CouponBusinessException;
import com.sweet.common.result.PageResult;
import com.sweet.coupon.common.CouponStatusEnum;
import com.sweet.coupon.entity.dto.CouponDTO;
import com.sweet.coupon.entity.dto.CouponPageDTO;
import com.sweet.coupon.entity.pojo.Coupon;
import com.sweet.coupon.entity.pojo.UserCoupon;
import com.sweet.coupon.entity.vo.AdminCouponVO;
import com.sweet.coupon.entity.vo.CouponVO;
import com.sweet.coupon.mapper.CouponMapper;
import com.sweet.coupon.mapper.UserCouponMapper;
import com.sweet.coupon.mq.event.CouponExecuteStatusEvent;
import com.sweet.coupon.mq.producer.CouponDelayExecuteStartProducer;
import com.sweet.coupon.mq.producer.CouponDelayExecuteEndProducer;
import com.sweet.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponDelayExecuteEndProducer endProducer;
    private final CouponDelayExecuteStartProducer startProducer;

    @Override
    public List<CouponVO> list() {

        List<Coupon> coupons = couponMapper.selectList(
                Wrappers.lambdaQuery(Coupon.class)
                        .eq(Coupon::getStatus, CouponStatusEnum.AVAILABLE.getCode())
                        .le(Coupon::getStartTime, LocalDateTime.now())
                        .ge(Coupon::getEndTime, LocalDateTime.now())
        );

        if (CollUtil.isEmpty(coupons)) {
            return List.of();
        }

        // 查询用户已领取的优惠券
        List<UserCoupon> userCoupons = userCouponMapper.selectList(
                Wrappers.lambdaQuery(UserCoupon.class)
                        .eq(UserCoupon::getUserId, BaseContext.getCurrentId())
        );

        Map<Long, Integer> receiveCountMap = userCoupons.stream()
                .collect(Collectors.groupingBy(
                        UserCoupon::getCouponId,
                        Collectors.summingInt(UserCoupon::getReceiveCount)
                ));


        // 过滤已领满的优惠券
        coupons = coupons.stream()
                .filter(coupon -> {
                    Integer receiveCount = receiveCountMap.get(coupon.getId());
                    return receiveCount == null
                            || receiveCount < coupon.getLimitPerUser();
                })
                .toList();

        return coupons.stream().map(coupon -> {
            CouponVO couponVO = BeanUtil.toBean(coupon, CouponVO.class);
            couponVO.setTags(StrUtil.splitTrim(coupon.getTags(), ","));
            return couponVO;
        }).toList();
    }

    @Override
    public PageResult page(CouponPageDTO couponPageDTO) {
        Page<AdminCouponVO> page = new Page<>(couponPageDTO.getPage(), couponPageDTO.getPageSize());

        IPage<AdminCouponVO> result = couponMapper.pageQuery(page, couponPageDTO);

        return PageResult.builder()
                .total(result.getTotal())
                .records(result.getRecords())
                .build();
    }

    @Override
    public void create(CouponDTO couponDTO) {
        Coupon coupon = BeanUtil.toBean(couponDTO, Coupon.class);
        String tags = StrUtil.join(",", couponDTO.getTags());
        coupon.setTags(tags);
        if (couponDTO.getDiscount() != null) {
            coupon.setDiscount(couponDTO.getDiscount() / 10);
        }

        if (Objects.equals(couponDTO.getStatus(), CouponStatusEnum.AVAILABLE.getCode())) {
            coupon.setStartTime(LocalDateTime.now());
        }

        LocalDateTime startTime = couponDTO.getStartTime();
        LocalDateTime endTime = couponDTO.getEndTime();
        long validDay = ChronoUnit.DAYS.between(startTime, endTime);
        validDay = Math.max(validDay, 1);
        coupon.setValidDay((int) validDay);

        int row = couponMapper.insert(coupon);

        if (row < 1) {
            throw new CouponBusinessException(MessageConstant.INSERT_ERROR);
        }

        // 延迟队列设置启用
        if (Objects.equals(coupon.getStatus(), CouponStatusEnum.UNAVAILABLE.getCode())) {
            long startTimeMilli = startTime.atZone(ZoneId.of("Asia/Shanghai"))
                    .toInstant()
                    .toEpochMilli();

            CouponExecuteStatusEvent event = CouponExecuteStatusEvent
                    .builder()
                    .couponId(coupon.getId())
                    .deliverTime(startTimeMilli)
                    .build();

            startProducer.sendMessage(event);
        }

        // 延迟队列设置过期
        long endTimeMilli = endTime.atZone(ZoneId.of("Asia/Shanghai"))
                .toInstant()
                .toEpochMilli();

        CouponExecuteStatusEvent event = CouponExecuteStatusEvent
                .builder()
                .couponId(coupon.getId())
                .deliverTime(endTimeMilli)
                .build();

        endProducer.sendMessage(event);

    }

    @Override
    public void update(CouponDTO couponDTO) {
        Coupon coupon = BeanUtil.toBean(couponDTO, Coupon.class);

        if (Objects.equals(coupon.getStatus(), CouponStatusEnum.AVAILABLE.getCode())) {
            throw new CouponBusinessException(MessageConstant.COUPON_UPDATE_ERROR_BY_START);
        }

        LocalDateTime now = LocalDateTime.now();

        // 检查是否在生效期内
        boolean isInEffectPeriod = now.isAfter(coupon.getStartTime())
                && now.isBefore(coupon.getEndTime());

        if (isInEffectPeriod) {
            throw new CouponBusinessException(MessageConstant.COUPON_UPDATE_STATUS_ERROR_BY_NOT_IN_TIME);
        }

        String tags = StrUtil.join(",", couponDTO.getTags());
        coupon.setTags(tags);

        int row = couponMapper.updateById(coupon);

        if (row < 1) {
            throw new CouponBusinessException(MessageConstant.UPDATE_ERROR);
        }
    }

    @Override
    public void startOrStop(Long id, Integer status) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new CouponBusinessException(MessageConstant.GET_COUPON_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();

        // 检查是否在生效期内
//        boolean isInEffectPeriod = now.isAfter(coupon.getStartTime())
//                && now.isBefore(coupon.getEndTime());
//
//        if (isInEffectPeriod) {
//            throw new CouponBusinessException(MessageConstant.COUPON_UPDATE_STATUS_ERROR_BY_NOT_IN_TIME);
//        }

        // 设置启用时间
        LocalDateTime endTime = null;
        if (Objects.equals(status, CouponStatusEnum.AVAILABLE.getCode())) {
            coupon.setStartTime(now);

            if (coupon.getValidDay() != null) {
                endTime = now.plusDays(coupon.getValidDay());
                coupon.setEndTime(endTime);
            }
        }


        coupon.setStatus(status);

        // 直接更新，不需要复杂条件
        int row = couponMapper.updateById(coupon);

        if (row < 1) {
            throw new CouponBusinessException(MessageConstant.UPDATE_ERROR);
        }

        if (Objects.equals(status, CouponStatusEnum.AVAILABLE.getCode())) {
            // 延迟队列设置过期
            long endTimeMills = endTime.atZone(ZoneId.of("Asia/Shanghai"))
                    .toInstant()
                    .toEpochMilli();

            CouponExecuteStatusEvent event = CouponExecuteStatusEvent
                    .builder()
                    .couponId(id)
                    .deliverTime(endTimeMills)
                    .build();

            endProducer.sendMessage(event);

        }
    }

    @Override
    public void delete(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new CouponBusinessException(MessageConstant.DELETE_ERROR);
        }

        // 查询要删除的优惠券
        List<Coupon> coupons = couponMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(coupons)) {
            throw new CouponBusinessException(MessageConstant.DELETE_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();

        // 只要有一张在生效期内，直接拒绝
        boolean hasCouponInEffect = coupons.stream().anyMatch(coupon ->
                coupon.getStartTime() != null
                        && coupon.getEndTime() != null
                        && now.isAfter(coupon.getStartTime())
                        && now.isBefore(coupon.getEndTime())
        );

        if (hasCouponInEffect) {
            throw new CouponBusinessException(
                    MessageConstant.COUPON_DELETE_ERROR_BY_IN_TIME
            );
        }

        int row = couponMapper.deleteBatchIds(ids);
        if (row < 1) {
            throw new CouponBusinessException(MessageConstant.DELETE_ERROR);
        }
    }

    @Override
    public AdminCouponVO getById(Long id) {
        if (id == null) {
            throw new CouponBusinessException(MessageConstant.DO_ERROR);
        }

        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new CouponBusinessException(MessageConstant.GET_COUPON_ERROR);
        }

        return BeanUtil.toBean(coupon, AdminCouponVO.class);
    }
}