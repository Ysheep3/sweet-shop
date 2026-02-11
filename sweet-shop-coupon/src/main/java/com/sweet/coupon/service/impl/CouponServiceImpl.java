package com.sweet.coupon.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.CouponBusinessException;
import com.sweet.common.result.PageResult;
import com.sweet.coupon.common.CouponRedisKey;
import com.sweet.coupon.common.CouponStatusEnum;
import com.sweet.coupon.common.CouponTypeEnum;
import com.sweet.coupon.entity.dto.CouponDTO;
import com.sweet.coupon.entity.dto.CouponPageDTO;
import com.sweet.coupon.entity.pojo.Coupon;
import com.sweet.coupon.entity.pojo.UserCoupon;
import com.sweet.coupon.entity.vo.AdminCouponVO;
import com.sweet.coupon.entity.vo.CouponVO;
import com.sweet.coupon.mapper.CouponMapper;
import com.sweet.coupon.mapper.UserCouponMapper;
import com.sweet.coupon.mq.event.CouponExecuteStatusEvent;
import com.sweet.coupon.mq.producer.CouponDelayExecuteEndProducer;
import com.sweet.coupon.mq.producer.CouponDelayExecuteStartProducer;
import com.sweet.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponDelayExecuteEndProducer endProducer;
    private final CouponDelayExecuteStartProducer startProducer;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<CouponVO> list() {
        // 1️⃣ Redis 中的可用优惠券 ID
        Set<String> couponIdSet = stringRedisTemplate.opsForSet()
                .members(CouponRedisKey.COUPON_ENABLE_KEY);

        if (CollUtil.isEmpty(couponIdSet)) {
            return List.of();
        }

        // 2️⃣ 查询用户已领取次数
        List<UserCoupon> userCoupons = userCouponMapper.selectList(
                Wrappers.lambdaQuery(UserCoupon.class)
                        .eq(UserCoupon::getUserId, BaseContext.getCurrentId())
        );

        Map<Long, Integer> receiveCountMap = userCoupons.stream()
                .collect(Collectors.groupingBy(
                        UserCoupon::getCouponId,
                        Collectors.summingInt(UserCoupon::getReceiveCount)
                ));

        // 3️⃣ pipeline 批量 HGETALL
        List<Object> couponMaps = stringRedisTemplate.executePipelined(
                (RedisCallback<Object>) connection -> {
                    for (String couponIdStr : couponIdSet) {
                        String couponKey = String.format(
                                CouponRedisKey.COUPON_KEY,
                                couponIdStr
                        );
                        connection.hGetAll(couponKey.getBytes());
                    }
                    return null;
                }
        );

        Iterator<String> idIterator = couponIdSet.iterator();
        List<CouponVO> result = new ArrayList<>();

        // 4️⃣ 组装结果
        for (Object obj : couponMaps) {
            String couponIdStr = idIterator.next();
            Long couponId = Long.valueOf(couponIdStr);

            @SuppressWarnings("unchecked")
            Map<String, String> rawMap = (Map<String, String>) obj;

            // 已过期，被 EXPIREAT 清掉
            if (CollUtil.isEmpty(rawMap)) {
                continue;
            }

            Map<String, Object> couponMap = new HashMap<>();
            rawMap.forEach((k, v) ->
                    couponMap.put(new String(k), new String(v))
            );

            Coupon coupon = BeanUtil.fillBeanWithMap(
                    couponMap,
                    new Coupon(),
                    false
            );

            // 5️⃣ 已领满过滤
            Integer receiveCount = receiveCountMap.get(couponId);
            if (receiveCount != null
                    && receiveCount >= coupon.getLimitPerUser()) {
                continue;
            }

            CouponVO couponVO = BeanUtil.toBean(coupon, CouponVO.class);
            couponVO.setTags(StrUtil.splitTrim(coupon.getTags(), ","));
            result.add(couponVO);
        }

        return result;
    }


//    @Override
//    public List<CouponVO> list() {
//
//        List<Coupon> coupons = couponMapper.selectList(
//                Wrappers.lambdaQuery(Coupon.class)
//                        .eq(Coupon::getStatus, CouponStatusEnum.AVAILABLE.getCode())
//                        .le(Coupon::getStartTime, LocalDateTime.now())
//                        .ge(Coupon::getEndTime, LocalDateTime.now())
//        );
//
//        if (CollUtil.isEmpty(coupons)) {
//            return List.of();
//        }
//
//        // 查询用户已领取的优惠券
//        List<UserCoupon> userCoupons = userCouponMapper.selectList(
//                Wrappers.lambdaQuery(UserCoupon.class)
//                        .eq(UserCoupon::getUserId, BaseContext.getCurrentId())
//        );
//
//        Map<Long, Integer> receiveCountMap = userCoupons.stream()
//                .collect(Collectors.groupingBy(
//                        UserCoupon::getCouponId,
//                        Collectors.summingInt(UserCoupon::getReceiveCount)
//                ));
//
//
//        // 过滤已领满的优惠券
//        coupons = coupons.stream()
//                .filter(coupon -> {
//                    Integer receiveCount = receiveCountMap.get(coupon.getId());
//                    return receiveCount == null
//                            || receiveCount < coupon.getLimitPerUser();
//                })
//                .toList();
//
//        return coupons.stream().map(coupon -> {
//            CouponVO couponVO = BeanUtil.toBean(coupon, CouponVO.class);
//            couponVO.setTags(StrUtil.splitTrim(coupon.getTags(), ","));
//            return couponVO;
//        }).toList();
//    }

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
        if (couponDTO.getName() == null) {
            throw new CouponBusinessException("优惠券名称不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        // 开始时间不能早于当前时间
        if (couponDTO.getStartTime() != null && couponDTO.getStartTime().isBefore(now)) {
            throw new CouponBusinessException(MessageConstant.COUPON_UPDATE_START_TIME_ERROR);
        }

        // 结束时间不能早于当前时间
        if (couponDTO.getEndTime() != null && couponDTO.getEndTime().isBefore(now)) {
            throw new CouponBusinessException(MessageConstant.COUPON_UPDATE_END_TIME_ERROR);
        }

        if (couponDTO.getLimitPerUser() == null) {
            throw new CouponBusinessException("每人限领不能为空");
        }

        if (couponDTO.getStock() == null) {
            throw new CouponBusinessException("库存不能为空");
        }

        if (couponDTO.getReduceAmount() == null) {
            throw new CouponBusinessException("减免金额不能为空");
        }

        if (couponDTO.getReduceAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CouponBusinessException("减免金额必须大于0");
        }

        if (couponDTO.getType().equals(CouponTypeEnum.FULL_REDUCE.getCode())) {
            if (couponDTO.getConditionAmount() == null) {
                throw new CouponBusinessException("满减券的满足条件金额和减免金额不能为空");
            }

            if (couponDTO.getConditionAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new CouponBusinessException("满足条件金额必须大于0");
            }

            if (couponDTO.getReduceAmount().compareTo(couponDTO.getConditionAmount()) >= 0) {
                throw new CouponBusinessException("减免金额必须小于满足条件金额");
            }
        }

        if (couponDTO.getType().equals(CouponTypeEnum.DISCOUNT.getCode())) {
            if (couponDTO.getDiscount() == null) {
                throw new CouponBusinessException("折扣券的折扣不能为空");
            }

            if (couponDTO.getDiscount() <= 0 || couponDTO.getDiscount() >= 10) {
                throw new CouponBusinessException("折扣必须在0~10之间");
            }
        }

        Coupon coupon = BeanUtil.toBean(couponDTO, Coupon.class);
        String tags = StrUtil.join(",", couponDTO.getTags());
        coupon.setTags(tags);
        if (couponDTO.getDiscount() != null) {
            coupon.setDiscount(couponDTO.getDiscount() / 10);
        }

        if (Objects.equals(couponDTO.getStatus(), CouponStatusEnum.AVAILABLE.getCode())) {
            coupon.setStartTime(now.withNano(0));
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

        // 存入缓存
        if (couponDTO.getStatus().equals(CouponStatusEnum.AVAILABLE.getCode())) {
            String couponKey = String.format(CouponRedisKey.COUPON_KEY, coupon.getId());
            Map<String, Object> couponMap = BeanUtil.beanToMap(coupon, false, true);
            Map<String, String> actrualCouponMap = couponMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> entry.getValue() != null ? entry.getValue().toString() : ""
                    ));

            List<String> args = new ArrayList<>(actrualCouponMap.size() * 2 + 1);
            actrualCouponMap.forEach((key, value) -> {
                args.add(key);
                args.add(value);
            });

            long endTimeMills = endTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
            String endTimeStr = String.valueOf(endTimeMills / 1000);
            args.add(endTimeStr);
            List<String> keys = Collections.singletonList(couponKey);

            String luaScript = "redis.call('HMSET', KEYS[1], unpack(ARGV, 1, #ARGV -1))" +
                    "redis.call('EXPIREAT', KEYS[1], ARGV[#ARGV])";

            stringRedisTemplate.execute(
                    new DefaultRedisScript<>(luaScript, Long.class),
                    keys,
                    args.toArray()
            );
        }
        // 加入“可查询索引”
        stringRedisTemplate.opsForSet().add(CouponRedisKey.COUPON_ENABLE_KEY, coupon.getId().toString());

//        RMap<String, Object> redisMap = redissonClient.getMap(couponKey);
//        Map<String, Object> filedMap = BeanUtil.beanToMap(
//                coupon,
//                new HashMap<>(),
//                CopyOptions.create()
//                        .setIgnoreNullValue(true)
//        );
//        if (couponDTO.getStatus().equals(CouponStatusEnum.AVAILABLE.getCode())) {
//            redisMap.putAll(filedMap);
//            long expireSeconds = Duration.between(coupon.getStartTime(), endTime).getSeconds();
//            if (expireSeconds > 0) {
//                redisMap.expire(expireSeconds, TimeUnit.SECONDS);
//            }
//            // 加入“可查询索引”
//            redissonClient.getSet(CouponRedisKey.COUPON_ENABLE_KEY).add(coupon.getId());
//        }

        // 延迟队列设置启用 存入缓存并设置过期时间
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
        Coupon oldCoupon = couponMapper.selectById(couponDTO.getId());
        if (oldCoupon == null) {
            throw new CouponBusinessException(MessageConstant.GET_COUPON_ERROR);
        }

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

        // 新的开始时间不能早于当前时间
        if (coupon.getStartTime() != null && coupon.getStartTime().isBefore(now)) {
            throw new CouponBusinessException(MessageConstant.COUPON_UPDATE_START_TIME_ERROR);
        }

        // 新的结束时间不能早于当前时间
        if (coupon.getEndTime() != null && coupon.getEndTime().isBefore(now)) {
            throw new CouponBusinessException(MessageConstant.COUPON_UPDATE_END_TIME_ERROR);
        }

        String tags = StrUtil.join(",", couponDTO.getTags());
        coupon.setTags(tags);



        boolean startTimeChanged =
                coupon.getStartTime() != null
                        && !coupon.getStartTime().isEqual(oldCoupon.getStartTime());

        boolean endTimeChanged =
                coupon.getEndTime() != null
                        && !coupon.getEndTime().isEqual(oldCoupon.getEndTime());

        // 只要开始或结束时间发生变化，就重新计算有效期天数
        if (startTimeChanged || endTimeChanged) {

            LocalDateTime startTime = coupon.getStartTime();
            LocalDateTime endTime = coupon.getEndTime();

            if (startTime != null && endTime != null) {
                long validDay = Duration.between(startTime, endTime).toDays();

                validDay = Math.max(validDay, 1);
                coupon.setValidDay((int) validDay);
            }
        }

        int row = couponMapper.updateById(coupon);

        if (row < 1) {
            throw new CouponBusinessException(MessageConstant.UPDATE_ERROR);
        }

        Long couponId = coupon.getId();

        // 重新发送消息
        if (startTimeChanged) {
            long startMillis = coupon.getStartTime()
                    .atZone(ZoneId.of("Asia/Shanghai"))
                    .toInstant()
                    .toEpochMilli();

            startProducer.sendMessage(
                    CouponExecuteStatusEvent.builder()
                            .couponId(couponId)
                            .deliverTime(startMillis)
                            .build()
            );
        }

        if (endTimeChanged) {
            long endMillis = coupon.getEndTime()
                    .atZone(ZoneId.of("Asia/Shanghai"))
                    .toInstant()
                    .toEpochMilli();

            endProducer.sendMessage(
                    CouponExecuteStatusEvent.builder()
                            .couponId(couponId)
                            .deliverTime(endMillis)
                            .build()
            );
        }
    }

    @Override
    public void startOrStop(Long id, Integer status) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new CouponBusinessException(MessageConstant.GET_COUPON_ERROR);
        }

        LocalDateTime now = LocalDateTime.now().withNano(0);

        // 启用优惠券
        if (Objects.equals(status, CouponStatusEnum.AVAILABLE.getCode())) {

            coupon.setStatus(status);
            coupon.setStartTime(now);

            LocalDateTime endTime = null;
            if (coupon.getValidDay() != null) {
                endTime = now.plusDays(coupon.getValidDay()).withNano(0);
                coupon.setEndTime(endTime);
            }

            int row = couponMapper.updateById(coupon);
            if (row < 1) {
                throw new CouponBusinessException(MessageConstant.UPDATE_ERROR);
            }

            // ===== Redis 写入 =====
            String couponKey = String.format(CouponRedisKey.COUPON_KEY, coupon.getId());

            Map<String, Object> couponMap = BeanUtil.beanToMap(coupon, false, true);
            Map<String, String> actualCouponMap = couponMap.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue() != null ? e.getValue().toString() : ""
                    ));

            List<String> args = new ArrayList<>(actualCouponMap.size() * 2 + 1);
            actualCouponMap.forEach((k, v) -> {
                args.add(k);
                args.add(v);
            });

            long endTimeMillis = endTime
                    .atZone(ZoneId.of("Asia/Shanghai"))
                    .toInstant()
                    .toEpochMilli();

            // EXPIREAT 使用秒级时间戳
            args.add(String.valueOf(endTimeMillis / 1000));

            String luaScript =
                    "redis.call('HMSET', KEYS[1], unpack(ARGV, 1, #ARGV - 1));" +
                            "redis.call('EXPIREAT', KEYS[1], ARGV[#ARGV]);";

            stringRedisTemplate.execute(
                    new DefaultRedisScript<>(luaScript, Long.class),
                    Collections.singletonList(couponKey),
                    args.toArray()
            );

            // ===== 加入“可用优惠券索引” =====
            stringRedisTemplate.opsForSet()
                    .add(CouponRedisKey.COUPON_ENABLE_KEY, id.toString());

            // ===== 投递延迟过期事件 =====
            CouponExecuteStatusEvent event = CouponExecuteStatusEvent.builder()
                    .couponId(id)
                    .deliverTime(endTimeMillis)
                    .build();

            endProducer.sendMessage(event);

            return;
        }

        // 停用优惠券
        coupon.setStatus(status);

        int row = couponMapper.updateById(coupon);
        if (row < 1) {
            throw new CouponBusinessException(MessageConstant.UPDATE_ERROR);
        }

        String couponKey = String.format(CouponRedisKey.COUPON_KEY, coupon.getId());

        // 1️⃣ 删除 Redis 中的优惠券缓存
        stringRedisTemplate.delete(couponKey);

        // 2️⃣ 从“可用索引集合”中移除
        stringRedisTemplate.opsForSet()
                .remove(CouponRedisKey.COUPON_ENABLE_KEY, id.toString());

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