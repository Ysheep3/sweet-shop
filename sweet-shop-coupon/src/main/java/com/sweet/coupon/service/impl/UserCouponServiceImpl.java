package com.sweet.coupon.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.CouponBusinessException;
import com.sweet.coupon.common.CouponRedisKey;
import com.sweet.coupon.common.CouponStatusEnum;
import com.sweet.coupon.common.UserCouponStatusEnum;
import com.sweet.coupon.entity.dto.UserCouponRedeemDTO;
import com.sweet.coupon.entity.dto.UserCouponDTO;
import com.sweet.coupon.entity.pojo.Coupon;
import com.sweet.coupon.entity.pojo.UserCoupon;
import com.sweet.coupon.entity.vo.UserCouponVO;
import com.sweet.coupon.mapper.CouponMapper;
import com.sweet.coupon.mapper.UserCouponMapper;
import com.sweet.coupon.mq.event.UserCouponRedeemEvent;
import com.sweet.coupon.mq.producer.UserCouponRedeemProducer;
import com.sweet.coupon.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl implements UserCouponService {

    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserCouponRedeemProducer userCouponRedeemProducer;
    private final RedissonClient redissonClient;

    private static final String LUA_PATH = "lua/stock_decrement_and_save_user_receive.lua";


    @Override
    public List<UserCouponVO> listByStatus(Integer status) {
        Long userId = BaseContext.getCurrentId();

        List<UserCouponDTO> userCouponDTOS;
        // 仅对“未使用”状态接入 Redis 缓存，其余状态仍然直接查库
        if (UserCouponStatusEnum.UNUSED.getCode().equals(status)) {
            String userCouponCacheKey = String.format(CouponRedisKey.USER_COUPON_KEY, userId);
            RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(userCouponCacheKey);
            // readAll 返回 Collection，这里统一转为 Set 便于后续处理
            Set<String> members = CollUtil.newHashSet(zset.readAll());

            if (CollUtil.isEmpty(members)) {
                // 缓存里没有，就直接回退到原来的 DB 查询
                userCouponDTOS = userCouponMapper.list(userId, status);
            } else {
                // 从 member 中解析出 userCouponId 列表：couponId_userCouponId
                List<Long> userCouponIds = new ArrayList<>(members.size());
                Map<Long, String> id2Member = new HashMap<>(members.size());
                for (String member : members) {
                    if (StrUtil.isBlank(member) || !member.contains("_")) {
                        continue;
                    }
                    String[] parts = member.split("_");
                    if (parts.length != 2) {
                        continue;
                    }
                    try {
                        Long userCouponId = Long.valueOf(parts[1]);
                        userCouponIds.add(userCouponId);
                        id2Member.put(userCouponId, member);
                    } catch (NumberFormatException ignore) {
                        // 忽略非法数据
                    }
                }

                if (CollUtil.isEmpty(userCouponIds)) {
                    userCouponDTOS = List.of();
                } else {
                    // 只从 DB 中查出“当前仍然有效且未使用”的券，SQL 会再做一次状态和时间过滤
                    userCouponDTOS = userCouponMapper.listByIds(userId, status, userCouponIds);

                    // 懒清理：如果 Redis 里有，但 DB 已经不符合条件（已用/过期），从 ZSet 中剔除
                    if (CollUtil.isNotEmpty(userCouponDTOS)) {
                        Set<Long> validIds = CollUtil.newHashSet(
                                userCouponDTOS.stream().map(UserCouponDTO::getId).toList()
                        );
                        for (Map.Entry<Long, String> entry : id2Member.entrySet()) {
                            if (!validIds.contains(entry.getKey())) {
                                zset.remove(entry.getValue());
                            }
                        }
                    } else {
                        // 全部失效，直接清空本地解析到的 members
                        for (String member : id2Member.values()) {
                            zset.remove(member);
                        }
                    }
                }
            }
        } else {
            userCouponDTOS = userCouponMapper.list(userId, status);
        }

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
    public void redeemCoupon(UserCouponRedeemDTO requestParam) {
        Long couponId = requestParam.getCouponId();
        Coupon coupon = couponMapper.selectOne(
                Wrappers.lambdaQuery(Coupon.class)
                        .eq(Coupon::getId, couponId)
                        .eq(Coupon::getStatus, CouponStatusEnum.AVAILABLE.getCode())
        );
        if (coupon == null) {
            throw new CouponBusinessException(MessageConstant.GET_COUPON_ERROR);
        }

        // 扣减缓存中库存
        @SuppressWarnings("rawtypes")
        DefaultRedisScript<List> redisScript = Singleton.get(LUA_PATH, () -> {
            @SuppressWarnings("rawtypes")
            DefaultRedisScript<List> script = new DefaultRedisScript<>();
            script.setResultType(List.class);
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource(LUA_PATH)));
            return script;
        });

        String couponCacheKey = String.format(CouponRedisKey.COUPON_KEY, couponId);
        String userCouponLimitCacheKey = String.format(CouponRedisKey.USER_COUPON_LIMIT_KEY, couponId, BaseContext.getCurrentId());
        List<String> keys = ListUtil.of(couponCacheKey, userCouponLimitCacheKey);
        String limitPerPerson = coupon.getLimitPerUser().toString();
        long endTime = coupon.getEndTime()
                .atZone(ZoneId.of("Asia/Shanghai"))
                .toInstant()
                .toEpochMilli();
        long currentTimeMillis = System.currentTimeMillis();
        long ttlMillis = endTime - currentTimeMillis;

        // 确保TTL不小于0
        if (ttlMillis <= 0) {
            ttlMillis = 3600000; // 默认1小时，单位毫秒
        }
        List<String> args = ListUtil.of(String.valueOf(ttlMillis), limitPerPerson);
        @SuppressWarnings("rawtypes")
        List result = stringRedisTemplate.execute(
                redisScript,
                keys,
                args.toArray()
        );
        Long code = (Long) result.get(0);
        Long count = (Long) result.get(1);

        if (code.intValue() == 1) {
            throw new CouponBusinessException(MessageConstant.COUPON_STOCK_NOT_ENOUGH);
        } else if (code.intValue() == 2) {
            throw new CouponBusinessException(MessageConstant.CLAIM_COUPON_LIMIT_ERROR);
        }

        UserCouponRedeemEvent event = UserCouponRedeemEvent.builder()
                .idempotent(UUID.randomUUID().toString())
                .coupon(coupon)
                .requestParam(requestParam)
                .receiveCount(count.intValue())
                .userId(BaseContext.getCurrentId())
                .build();

        userCouponRedeemProducer.sendMessage(event);

//        List<UserCoupon> userCoupons = userCouponMapper.selectList(
//                Wrappers.lambdaQuery(UserCoupon.class)
//                        .eq(UserCoupon::getUserId, BaseContext.getCurrentId())
//                        .eq(UserCoupon::getCouponId, couponId)
//        );
//
//        int receiveCount;
//        if (CollUtil.isNotEmpty(userCoupons)) {
//            // 判断是否超过用户领取限制
//            receiveCount = userCoupons.stream()
//                    .map(UserCoupon::getReceiveCount)
//                    .reduce(0, Integer::sum);
//
//            if (receiveCount >= coupon.getLimitPerUser()) {
//                throw new CouponBusinessException(MessageConstant.CLAIM_COUPON_LIMIT_ERROR);
//            }
//
//            receiveCount = receiveCount + 1;
//            UserCoupon userCouponUpd = userCoupons.get(userCoupons.size() - 1);
//            userCouponUpd.setReceiveCount(receiveCount);
//
//            if (userCouponUpd.getStatus().equals(UserCouponStatusEnum.UNUSED.getCode())) {
//                userCouponMapper.updateById(userCouponUpd);
//            } else {
//                // 如果最后一张优惠券状态不是未使用，则新增一张优惠券记录
//                userCouponUpd = BeanUtil.toBean(requestParam, UserCoupon.class);
//                userCouponUpd.setStatus(UserCouponStatusEnum.UNUSED.getCode());
//                userCouponUpd.setUserId(BaseContext.getCurrentId());
//                userCouponUpd.setReceiveTime(LocalDateTime.now());
//                userCouponUpd.setReceiveCount(1);
//
//                userCouponMapper.insert(userCouponUpd);
//            }
//            Integer stock = coupon.getStock();
//
//            couponMapper.update(coupon,
//                    Wrappers.lambdaUpdate(Coupon.class)
//                            .ge(Coupon::getStock, 1)
//                            .set(Coupon::getStock, stock - 1)
//            );
//            // 延迟队列设置过期
//            sendDelayMessage(userCouponUpd);
//            return;
//        }
//
//        UserCoupon userCoupon = BeanUtil.toBean(requestParam, UserCoupon.class);
//        userCoupon.setStatus(UserCouponStatusEnum.UNUSED.getCode());
//        userCoupon.setUserId(BaseContext.getCurrentId());
//        userCoupon.setReceiveTime(LocalDateTime.now());
//        userCoupon.setReceiveCount(1);
//
//        userCouponMapper.insert(userCoupon);

        // 延迟队列设置过期
    }

    @Override
    public void useCoupon(Long id) {
        if (id == null) {
            throw new CouponBusinessException(MessageConstant.DO_ERROR);
        }

        String userCouponLockKey = String.format(CouponRedisKey.LOCK_USER_COUPON_KEY, id);
        RLock lock = redissonClient.getLock(userCouponLockKey);
        if (!lock.tryLock()) {
            // 该优惠券正在使用
            throw new CouponBusinessException(MessageConstant.COUPON_IS_USING);
        }

        UserCoupon userCoupon;
        try {
            userCoupon = userCouponMapper.selectOne(
                    Wrappers.lambdaQuery(UserCoupon.class)
                            .eq(UserCoupon::getId, id)
                            .eq(UserCoupon::getUserId, BaseContext.getCurrentId())
            );

            if (userCoupon == null) {
                throw new CouponBusinessException(MessageConstant.GET_COUPON_ERROR);
            }

            if (userCoupon.getEndTime().isBefore(LocalDateTime.now())) {
                throw new CouponBusinessException(MessageConstant.COUPON_HAS_EXPIRED);
            }

            if (!userCoupon.getStatus().equals(UserCouponStatusEnum.UNUSED.getCode())) {
                throw new CouponBusinessException("优惠券状态异常");
            }

            userCoupon.setStatus(UserCouponStatusEnum.USED.getCode());
            userCoupon.setUseTime(LocalDateTime.now());
            userCouponMapper.updateById(userCoupon);

            // 删除缓存
            String userCouponCacheKey = String.format(CouponRedisKey.USER_COUPON_KEY, BaseContext.getCurrentId());
            String value = StrUtil.builder()
                    .append(userCoupon.getCouponId())
                    .append("_")
                    .append(userCoupon.getId()).toString();

            // 同 Consumer：规避 Spring Data Redis 2.4.3 在 JDK17 下对 ZSet 操作可能触发的递归 StackOverflowError
            RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(userCouponCacheKey);
            zset.remove(value);
        } finally {
            lock.unlock();
        }
    }
}
