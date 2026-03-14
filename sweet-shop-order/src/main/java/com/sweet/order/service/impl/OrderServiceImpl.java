package com.sweet.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sweet.api.client.*;
import com.sweet.api.dto.*;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.AddressBookBusinessException;
import com.sweet.common.exception.BaseException;
import com.sweet.common.exception.OrderBusinessException;
import com.sweet.common.properties.AlipayProperties;
import com.sweet.common.properties.AmapProperties;
import com.sweet.common.properties.ShopProperties;
import com.sweet.common.result.PageResult;
import com.sweet.common.result.Result;
import com.sweet.common.utils.HttpClientUtil;
import com.sweet.order.common.OrderPayStatusEnum;
import com.sweet.order.common.OrderStatusEnum;
import com.sweet.order.common.OrderTypeEnum;
import com.sweet.order.entity.dto.*;
import com.sweet.order.entity.dto.RiderOrderStatDTO;
import com.sweet.order.entity.pojo.Order;
import com.sweet.order.entity.pojo.OrderDetail;
import com.sweet.order.entity.vo.*;
import com.sweet.order.mapper.OrderDetailMapper;
import com.sweet.order.mapper.OrderMapper;
import com.sweet.order.mq.event.AdminDispatchOrderEvent;
import com.sweet.order.mq.producer.AdminDispatchOrderProducer;
import com.sweet.order.service.OrderService;
import com.sweet.order.websocket.RiderWebSocketServer;
import com.sweet.order.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final UserClient userClient;
    private final CartClient cartClient;
    private final CouponClient couponClient;
    private final UserCouponClient userCouponClient;
    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;
    private final WebSocketServer webSocketServer;
    private final RiderWebSocketServer riderWebSocketServer;
    private final AmapProperties amapProperties;
    private final ShopProperties shopProperties;
    private final RedissonClient redissonClient;
    private final AdminDispatchOrderProducer adminDispatchOrderProducer;

    // 配送费 3元
    private static final BigDecimal ShippingFees = BigDecimal.valueOf(3);
    // 假设骑手每单收入1元
    private static final BigDecimal RiderIncome = BigDecimal.valueOf(2);
    private final EmployeeClient employeeClient;
    private final StringRedisTemplate stringRedisTemplate;


    @Override
    @Transactional
    public OrderPayVO createOrder(OrderDTO requestParam) {
        if (requestParam == null) {
            throw new OrderBusinessException(MessageConstant.DO_ERROR);
        }

        Integer orderType = requestParam.getOrderType();
        String userLocation = "";
        if (Objects.equals(orderType, OrderTypeEnum.DELIVERY.getCode())) {
            // 外卖订单 需要校验地址和距离
            Long addressId = requestParam.getAddressId();
            Result<AddressVO> addressResult = userClient.getById(addressId);
            if (addressResult.getData() == null) {
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_IS_NULL);
            }

            // 获取两地经纬度
            String userAddress = requestParam.getAddress();
            String shopAddress = shopProperties.getAddress();

            // 地址 → 经纬度
            userLocation = getLocation(userAddress);
            String shopLocation = stringRedisTemplate.opsForValue().get("shop_location");
//            RBucket<String> bucket = redissonClient.getBucket("shop_location");
//
//            String shopLocation = bucket.get();

            if (StrUtil.isBlank(shopLocation)) {
                shopLocation = getLocation(shopAddress); // 调高德 geocode
                stringRedisTemplate.opsForValue().set("shop_location", shopLocation);
            }

            // 计算距离 大于5000则抛异常
            Integer distance = calculateDistance(userLocation, shopLocation);
            if (distance > 5000) {
                throw new OrderBusinessException(MessageConstant.ORDERS_DISTANCE_ERROR);
            }
        }


        List<Long> ids = requestParam.getCartItems().stream().map(ShoppingCartVO::getId).toList();
        Result<List<ShoppingCartVO>> cartResult = cartClient.listByIds(ids);
        List<ShoppingCartVO> cartVOList = cartResult.getData();
        if (CollUtil.isEmpty(cartVOList)) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        if (cartVOList.size() != ids.size()) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_SOME_IS_NULL);
        }


        // 计算金额是否正确
        calculate(requestParam);

        Order order = BeanUtil.toBean(requestParam, Order.class);

        order.setUserId(BaseContext.getCurrentId());
        if (requestParam.getUserCoupon() != null) {
            order.setUserCouponId(requestParam.getUserCoupon().getId());
        }
        order.setStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
        order.setOrderTime(LocalDateTime.now());
        order.setPayStatus(OrderPayStatusEnum.UN_PAID.getCode());
        order.setOrderNo(String.valueOf(System.currentTimeMillis()));
        order.setLocation(userLocation);
        orderMapper.insert(order);
        log.info("订单id: {}", order.getId());

        List<OrderDetail> orderDetails = new ArrayList<>();
        cartVOList.forEach(cart -> {
            OrderDetail orderDetail = BeanUtil.toBean(cart, OrderDetail.class);
            orderDetail.setOrderId(order.getId());
            orderDetails.add(orderDetail);
        });

        orderDetailMapper.insertBatchs(orderDetails);
        // 优惠券使用后更新状态
        if (requestParam.getUserCoupon() != null) {
            userCouponClient.useCoupon(requestParam.getUserCoupon().getId());
        }
        // 添加成功后 删除购物车所选商品
        cartClient.deleteByIds(ids);

        OrderPayVO orderPayVO = OrderPayVO.builder()
                .id(order.getId().toString())
                .orderAmount(order.getAmount())
                .orderNo(order.getOrderNo())
                .consignee(order.getConsignee())
                .build();

        return orderPayVO;
    }

    private Integer calculateDistance(String origin, String destination) {
        try {
            String url = "https://restapi.amap.com/v3/distance";

            Map<String, String> params = new HashMap<>();
            params.put("key", amapProperties.getKey());
            params.put("origins", origin);
            params.put("destination", destination);
            params.put("type", "0");

            String result = HttpClientUtil.doGet(url, params);

            if (StrUtil.isBlank(result)) {
                throw new OrderBusinessException("距离计算失败：API返回空响应");
            }

            JSONObject json = JSONUtil.parseObj(result);

            // 检查高德 API 状态
            if (!"1".equals(json.getStr("status"))) {
                String errorInfo = json.getStr("info", "未知错误");
                throw new OrderBusinessException("距离计算失败：" + errorInfo);
            }

            JSONArray results = json.getJSONArray("results");
            if (CollUtil.isEmpty(results)) {
                throw new OrderBusinessException("距离计算失败：未获取到距离信息");
            }

            JSONObject firstResult = results.getJSONObject(0);
            Integer distance = firstResult.getInt("distance");

            if (distance == null) {
                throw new OrderBusinessException("距离计算失败：距离数据为空");
            }

            return distance;

        } catch (JSONException e) {
            log.error("距离计算数据解析异常，起点：{}，终点：{}", origin, destination, e);
            throw new OrderBusinessException("距离计算失败：数据解析异常");
        } catch (OrderBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("距离计算未知异常，起点：{}，终点：{}", origin, destination, e);
            throw new OrderBusinessException("距离计算失败：系统异常");
        }
    }

    private String getLocation(String address) {
        try {
            String url = "https://restapi.amap.com/v3/geocode/geo";

            Map<String, String> params = new HashMap<>();
            params.put("key", amapProperties.getKey());
            params.put("address", address);

            String result = HttpClientUtil.doGet(url, params);
            JSONObject json = JSONUtil.parseObj(result);

            JSONArray geocodes = json.getJSONArray("geocodes");
            if (CollUtil.isEmpty(geocodes)) {
                throw new OrderBusinessException("高德地理编码失败");
            }

            // 返回格式：lng,lat
            return geocodes.getJSONObject(0).getStr("location");

        } catch (Exception e) {
            log.error("高德地理编码异常", e);
            throw new OrderBusinessException("地址解析失败");
        }
    }

    private void calculate(OrderDTO requestParam) {
        BigDecimal orderAmount = requestParam.getAmount();
        UserCouponDTO userCoupon = requestParam.getUserCoupon();
        List<ShoppingCartVO> cartItems = requestParam.getCartItems();
        BigDecimal itemsAmount = cartItems.stream()
                .map(item -> item.getAmount().multiply(
                        BigDecimal.valueOf(item.getNumber())
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (userCoupon != null) {
            Result<CouponVO> result = couponClient.getById(userCoupon.getCouponId());
            CouponVO couponVO = result.getData();
            if (Objects.equals(couponVO.getType(), 1)) {
                // 满减券
                if (itemsAmount.compareTo(couponVO.getConditionAmount()) < 0) {
                    throw new OrderBusinessException(MessageConstant.COUPON_CANNOT_USE);
                }
                itemsAmount = itemsAmount.subtract(couponVO.getReduceAmount());
            } else if (Objects.equals(couponVO.getType(), 2)) {
                // 折扣券
                BigDecimal discount = BigDecimal.valueOf(couponVO.getDiscount());

                itemsAmount = itemsAmount
                        .multiply(discount)
                        .setScale(2, RoundingMode.HALF_UP);

            } else if (Objects.equals(couponVO.getType(), 3)) {
                // 无门槛
                itemsAmount = itemsAmount.subtract(couponVO.getReduceAmount());
            }
        }
        BigDecimal totalAmount = BigDecimal.valueOf(0);
        if (Objects.equals(requestParam.getOrderType(), OrderTypeEnum.DELIVERY.getCode())) {
            totalAmount = itemsAmount.add(ShippingFees);
        } else {
            totalAmount = itemsAmount;
        }

        // 金额下限保护
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        if (orderAmount.compareTo(totalAmount) != 0) {
            throw new OrderBusinessException(MessageConstant.ORDERS_AMOUNT_DIFF);
        }
    }

    @Override
    public List<OrderVO> listByStatus(List<Integer> statusList) {
        List<Order> orders = new ArrayList<>();

        if (CollUtil.isEmpty(statusList)) {
            orders = orderMapper.selectList(
                    Wrappers.lambdaQuery(Order.class)
                            .eq(Order::getUserId, BaseContext.getCurrentId())
                            .orderByDesc(Order::getOrderTime)
            );
        } else {
            orders = orderMapper.selectList(
                    Wrappers.lambdaQuery(Order.class)
                            .eq(Order::getUserId, BaseContext.getCurrentId())
                            .in(Order::getStatus, statusList)
                            .orderByDesc(Order::getOrderTime)
            );
        }

        if (CollUtil.isEmpty(orders)) {
            return List.of();
        }
        List<OrderVO> orderVOS = new ArrayList<>();
        for (Order order : orders) {
            OrderVO orderVO = BeanUtil.toBean(order, OrderVO.class);
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(
                    Wrappers.lambdaQuery(OrderDetail.class)
                            .eq(OrderDetail::getOrderId, order.getId())
            );
            orderVO.setOrderDetails(orderDetails);
            orderVOS.add(orderVO);
        }

        return orderVOS;
    }

    @Override
    public void pay(OrderPayDTO requestParam) {
        String orderNo = requestParam.getOrderNo();
        if (StrUtil.isBlank(orderNo)) {
            throw new OrderBusinessException(MessageConstant.ORDERS_ID_IS_NULL);
        }
        // ===== 2. 查订单   =====
        Order order = orderMapper.selectOne(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getUserId, BaseContext.getCurrentId())
                        .eq(Order::getOrderNo, orderNo)
        );
        if (order == null) throw new OrderBusinessException(MessageConstant.ORDERS_IS_NULL);
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.PENDING_PAYMENT.getCode())) {
            throw new OrderBusinessException(MessageConstant.ORDERS_STATUS_ERROR);
        }

        // ===== 3. 模拟支付成功 =====
        paySuccess(order);
    }

    public void paySuccess(Order order) {
        order.setPayTime(LocalDateTime.now());
        order.setStatus(OrderStatusEnum.PENDING_ACCEPTANCE.getCode());
        order.setPayStatus(OrderPayStatusEnum.PAID.getCode());

        orderMapper.updateById(order);

        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", order.getId().toString());
        map.put("content", "订单号:" + order.getOrderNo());

        String message = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(message);
    }

    /**
     * 统计各个状态下的订单数量
     *
     * @return
     */
    public OrderCountVO countByStatus(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(Order::getUserId, BaseContext.getCurrentId());
        }

        wrapper.eq(Order::getStatus, OrderStatusEnum.PENDING_PAYMENT.getCode());
        Long pendingPayment = orderMapper.selectCount(wrapper);

        wrapper.clear();
        wrapper.eq(Order::getStatus, OrderStatusEnum.PENDING_ACCEPTANCE.getCode());
        Long toBeConfirmed = orderMapper.selectCount(wrapper);

        wrapper.clear();
        wrapper.eq(Order::getStatus, OrderStatusEnum.ACCEPTED.getCode());
        Long confirmed = orderMapper.selectCount(wrapper);

        wrapper.clear();
        wrapper.eq(Order::getStatus, OrderStatusEnum.IN_DELIVERY.getCode());
        Long deliveryInProgress = orderMapper.selectCount(wrapper);

        wrapper.clear();
        wrapper.eq(Order::getStatus, OrderStatusEnum.PENDING_PICKUP.getCode());
        Long toBePickUp = orderMapper.selectCount(wrapper);

        wrapper.clear();
        wrapper.eq(Order::getStatus, OrderStatusEnum.PICKING_UP.getCode());
        Long pickingUp = orderMapper.selectCount(wrapper);

        return OrderCountVO.builder()
                .pickingUp(pickingUp)
                .toBePickUp(toBePickUp)
                .pendingPayment(pendingPayment)
                .toBeConfirmed(toBeConfirmed)
                .confirmed(confirmed)
                .deliveryInProgress(deliveryInProgress)
                .build();
    }

    /**
     * 根据id查询订单详情
     *
     * @param id
     * @return
     */
    public OrderVO getOrderWithDetailsById(Long id) {
        Order order = orderMapper.selectById(id);
        OrderVO orderVO = BeanUtil.copyProperties(order, OrderVO.class);

        List<OrderDetail> details = new LambdaQueryChainWrapper<>(orderDetailMapper)
                .eq(OrderDetail::getOrderId, id)
                .list();

        orderVO.setOrderDetails(details);
        return orderVO;
    }

    /**
     * 订单搜索
     *
     * @param ordersPageDTO
     * @return
     */
    public PageResult pageQuery(OrdersPageDTO ordersPageDTO) {
        Page<OrderVO> page = new Page<>(ordersPageDTO.getPage(), ordersPageDTO.getPageSize());

        IPage<OrderVO> result = orderMapper.pageQuery(page, ordersPageDTO);

        List<OrderVO> orderVOList = result.getRecords();
        for (OrderVO orderVO : orderVOList) {

            String orderDishes = getOrderDishesStr(orderVO);
            orderVO.setOrderDishes(orderDishes);
        }

        return new PageResult(result.getTotal(), orderVOList);
    }

    @Override
    @Transactional
    public void confirm(OrderDTO orderDTO) {
        Order order = getById(orderDTO);
        order.setStatus(OrderStatusEnum.ACCEPTED.getCode());
        orderMapper.updateById(order);

        // 接完单之后 通过算法派单给骑手
        // 获取店内所有骑手
        Result<List<Long>> result = employeeClient.getRiderIds();
        List<Long> ids = result.getData();
        String key = "rider:location:";
        String shopLocation = stringRedisTemplate.opsForValue().get("shop_location");
        if (StrUtil.isBlank(shopLocation)) {
            shopLocation = getLocation(shopProperties.getAddress());
            stringRedisTemplate.opsForValue().set("shop_location", shopLocation);
        }
        Map<Long, Double> scoreMap = new HashMap<>();
        for (Long id : ids) {
            // 排除不在线的骑手
            if (!stringRedisTemplate.hasKey(key + id)) {
                continue;  // ❗ 不在线，跳过
            }
            // 查询该骑手正在派送的单子
            List<Order> orders = orderMapper.selectList(
                    Wrappers.lambdaQuery(Order.class)
                            .eq(Order::getDeliveryEmployeeId, id)
                            .eq(Order::getStatus, OrderStatusEnum.IN_DELIVERY.getCode())
                            .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
                            .orderByAsc(Order::getOrderTime)
            );

            if (orders.size() >= 10) {
                continue;  // ❗ 已满 10 单
            }

            // 获取骑手当前位置
            Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(key + id);
            if (map.isEmpty()) {
                continue;  // ❗ 位置异常，跳过
            }

            String longitude = (String) map.get("longitude");
            String latitude = (String) map.get("latitude");
            if (longitude == null || latitude == null) {
                continue;
            }

            String currentLocation  = longitude + "," + latitude;
            double totalDistance = 0;
            for (Order o : orders) {
                String userLocation = o.getLocation();
                double distance = localCalculateDistance(currentLocation , userLocation);
                totalDistance += distance;
                currentLocation = userLocation;
            }

            double shopToRiderDistance = localCalculateDistance(shopLocation, currentLocation);
            // score = dShop + dCurrent + (orderCount * 1000);
            double score = shopToRiderDistance + totalDistance + (orders.size() * 1000);
            scoreMap.put(id, score);
        }
        if (scoreMap.isEmpty()) {
            throw new BaseException("暂无可派送骑手");
        }

        List<Map.Entry<Long, Double>> entries =
                new ArrayList<>(scoreMap.entrySet());

        // 先随机打乱
        Collections.shuffle(entries);
        // 再排序（稳定排序）
        entries.sort(Map.Entry.comparingByValue());
        // 按 score 升序排序
        List<Long> sortedRiders = entries.stream()
                .map(Map.Entry::getKey)
                .toList();

        String dispatchKey = "dispatch:order:" + order.getId();

        stringRedisTemplate.opsForValue().set(
                dispatchKey,
                JSON.toJSONString(sortedRiders),
                2, TimeUnit.MINUTES
        );

        Long firstRider = sortedRiders.get(0);
        Map<String, Object> map = BeanUtil.beanToMap(order);
        String message = JSON.toJSONString(map);

        riderWebSocketServer.sendToRider(firstRider, message);

        AdminDispatchOrderEvent event = AdminDispatchOrderEvent.builder()
                .orderId(order.getId())
                .redisKey(dispatchKey)
                .deliverTime(System.currentTimeMillis() + 30000)
                .build();

        adminDispatchOrderProducer.sendMessage(event);
    }

    private double localCalculateDistance(String origin, String destination) {
        String[] p1 = origin.split(",");
        String[] p2 = destination.split(",");

        double riderLon = Double.parseDouble(p1[0]);
        double riderLat = Double.parseDouble(p1[1]);
        double userLon = Double.parseDouble(p2[0]);
        double userLat = Double.parseDouble(p2[1]);

        final int R = 6371000; // 地球半径（米）
        double dLat = Math.toRadians(userLat - riderLat);
        double dLon = Math.toRadians(userLon - riderLon);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(riderLat)) *
                        Math.cos(Math.toRadians(userLat)) *
                        Math.sin(dLon / 2) *
                        Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // 单位：米
    }

    @Override
    public void rejection(OrderDTO orderDTO) {
        Order order = getById(orderDTO);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(orderDTO.getCancelReason());
        order.setStatus(OrderStatusEnum.CANCELED.getCode());

        orderMapper.updateById(order);
    }

    @Override
    public void cancel(OrderDTO orderDTO) {
        Order order = getById(orderDTO);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(orderDTO.getCancelReason());
        order.setStatus(OrderStatusEnum.CANCELED.getCode());
        orderMapper.updateById(order);
    }

    @Override
    public void cancel(Long id) {
        Order orders = Order.builder()
                .id(id)
                .cancelTime(LocalDateTime.now())
                .status(OrderStatusEnum.CANCELED.getCode())
                .build();

        orderMapper.updateById(orders);
    }

    @Override
    public void reminder(Long id) {
        Order order = orderMapper.selectById(id);

        if (order == null) {
            throw new OrderBusinessException("订单异常");
        }

        String orderNo = order.getOrderNo();

        // 前端 json 格式接收
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);
        map.put("orderId", id.toString());
        map.put("content", "订单号为" + orderNo + "的订单催单了!");

        String message = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(message);
    }

    @Override
    public void again(Long id) {
        List<OrderDetail> details = new LambdaQueryChainWrapper<>(orderDetailMapper)
                .eq(OrderDetail::getOrderId, id)
                .list();

        List<ShoppingCartVO> shoppingCartList = new ArrayList<>();
        for (OrderDetail detail : details) {
            ShoppingCartVO cart = BeanUtil.copyProperties(detail, ShoppingCartVO.class);
            shoppingCartList.add(cart);

        }
        cartClient.again(shoppingCartList);
    }


//    @Override
//    public void delivery(Long id) {
//        Order order = getById(id);
//        order.setStatus(OrderStatusEnum.IN_DELIVERY.getCode());
//        orderMapper.updateById(order);
//    }

    @Override
    public void complete(Long id) {
        Order order = getById(id);
        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setDeliveryTime(LocalDateTime.now());

        orderMapper.updateById(order);
    }

    @Override
    public BusinessDataVO getBusinessData(LocalDateTime beginTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(Order::getOrderTime, beginTime);
        wrapper.lt(Order::getOrderTime, endTime);

        Long totalOrderCount = orderMapper.selectCount(wrapper);

        wrapper.eq(Order::getStatus, OrderStatusEnum.COMPLETED.getCode());
        Long validOrderCount = orderMapper.selectCount(wrapper);

        double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
        }

        List<Order> orders = orderMapper.selectList(wrapper);
        BigDecimal turnover = BigDecimal.valueOf(0);
        if (orders != null && !orders.isEmpty()) {
            turnover = orders.stream().map(Order::getAmount).reduce(BigDecimal::add).get();
        }

        double unitPrice = 0.0;
        if (validOrderCount != 0) {
            unitPrice = (double) (turnover.longValue() / validOrderCount);
        }

        BusinessDataVO vo = new BusinessDataVO();
        vo.setTurnover(turnover.doubleValue());
        vo.setUnitPrice(unitPrice);
        vo.setOrderCompletionRate(orderCompletionRate);
        vo.setValidOrderCount(validOrderCount.intValue());

        return vo;
    }

    @Override
    public OrderOverViewVO getOverviewOrders() {
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(today, LocalTime.MAX);

        return orderMapper.countOrderByStatus(beginTime, endTime);
    }

    @Override
    public OrderReportVO getOrdersStatistics(OrderReportDTO orderReportDTO) {
        LocalDate begin = orderReportDTO.getBegin();
        LocalDate end = orderReportDTO.getEnd();

        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        // 统计每一天的订单总数 有效订单数
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.gt(Order::getOrderTime, beginTime);
            wrapper.lt(Order::getOrderTime, endTime);
            Long totalOrderCount = orderMapper.selectCount(wrapper);
            orderCountList.add(totalOrderCount.intValue());

            wrapper.eq(Order::getStatus, OrderStatusEnum.COMPLETED.getCode());
            Long validOrderCount = orderMapper.selectCount(wrapper);
            validOrderCountList.add(validOrderCount.intValue());
        }

        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
        }

        OrderReportVO vo = new OrderReportVO();
        vo.setTotalOrderCount(totalOrderCount);
        vo.setValidOrderCount(validOrderCount);
        vo.setOrderCompletionRate(orderCompletionRate);
        vo.setOrderCountList(StringUtils.join(validOrderCountList, ","));
        vo.setDateList(StringUtils.join(dateList, ","));
        vo.setValidOrderCountList(StringUtils.join(validOrderCountList, ","));

        return vo;
    }

    @Override
    public TurnoverReportVO getTurnoverStatistics(OrderReportDTO orderReportDTO) {
        LocalDate begin = orderReportDTO.getBegin();
        LocalDate end = orderReportDTO.getEnd();

        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<BigDecimal> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.gt(Order::getOrderTime, beginTime);
            wrapper.lt(Order::getOrderTime, endTime);
            wrapper.eq(Order::getStatus, OrderStatusEnum.COMPLETED.getCode());

            List<Order> orders = orderMapper.selectList(wrapper);
            BigDecimal bigDecimal = BigDecimal.valueOf(0);
            if (orders != null && !orders.isEmpty()) {
                bigDecimal = orders.stream().map(Order::getAmount).reduce(BigDecimal::add).get();
            }
            turnoverList.add(bigDecimal);

        }

        TurnoverReportVO vo = new TurnoverReportVO();
        vo.setTurnoverList(StringUtils.join(turnoverList, ","));
        vo.setDateList(StringUtils.join(dateList, ","));

        return vo;
    }

    @Override
    public SalesTop10ReportVO getTop10(OrderReportDTO orderReportDTO) {
        LocalDateTime beginTime = LocalDateTime.of(orderReportDTO.getBegin(), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(orderReportDTO.getEnd(), LocalTime.MAX);

        List<Long> orderIds = new LambdaQueryChainWrapper<>(orderMapper)
                .lt(Order::getOrderTime, endTime)
                .gt(Order::getOrderTime, beginTime)
                .eq(Order::getStatus, OrderStatusEnum.COMPLETED.getCode())
                .list()
                .stream().map(Order::getId).toList();

        List<OrderDetail> details = new ArrayList<>();

        if (!orderIds.isEmpty()) {
            details = orderDetailMapper.getTop10(orderIds);
        }

        List<String> nameList = details.stream().map(OrderDetail::getName).toList();
        List<Integer> numberList = details.stream().map(OrderDetail::getNumber).toList();

        SalesTop10ReportVO vo = new SalesTop10ReportVO();
        vo.setNameList(StringUtils.join(nameList, ","));
        vo.setNumberList(StringUtils.join(numberList, ","));


        return vo;
    }

    @Override
    public OrderVO getByOrderNo(String orderNo) {
        Order order = orderMapper.selectOne(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getUserId, BaseContext.getCurrentId()));

        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDERS_IS_NULL);
        }

        List<OrderDetail> orderDetails = orderDetailMapper.selectList(
                Wrappers.lambdaQuery(OrderDetail.class)
                        .eq(OrderDetail::getOrderId, order.getId()));

        OrderVO vo = BeanUtil.toBean(order, OrderVO.class);
        vo.setOrderDetails(orderDetails);
        return vo;
    }

    /**
     * 获取订单中详情str格式:
     * <p>
     * 菜品: 宫保鸡丁(口味)*3;
     * 套餐: 人气套餐*3;
     */
    private String getOrderDishesStr(OrderVO orderVO) {
        List<OrderDetail> details = new LambdaQueryChainWrapper<>(orderDetailMapper)
                .eq(OrderDetail::getOrderId, orderVO.getId())
                .list();

        List<String> orderDishesStr = details.stream()
                .map(detail -> detail.getName() + "*" + detail.getNumber() + ";")
                .toList();

        return String.join(" ", orderDishesStr);
    }

    @NonNullDecl
    private Order getById(OrderDTO orderDTO) {
        Order order = orderMapper.selectById(orderDTO.getId());
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDERS_IS_NULL);
        }
        return order;
    }

    @NonNullDecl
    private Order getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDERS_IS_NULL);
        }
        return order;
    }

    @Override
    public List<OrderVO> getByStatus(String type) { // // wait | accepted | delivery | completed
        if (StrUtil.isBlank(type)) {
            throw new OrderBusinessException(MessageConstant.DO_ERROR);
        }
        List<Order> orders = new ArrayList<>();
        List<Integer> status = new ArrayList<>();
        if ("wait".equals(type)) {
            // 查询 rider_id IS NULL AND status IN (3,7)
            status.add(OrderStatusEnum.ACCEPTED.getCode());
            status.add(OrderStatusEnum.PENDING_PICKUP.getCode());

            orders = orderMapper.selectList(
                    Wrappers.lambdaQuery(Order.class)
                            .isNull(Order::getDeliveryEmployeeId)
                            .in(Order::getStatus, status)
                            .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
                            .orderByDesc(Order::getOrderTime)
            );
        }

        if ("accepted".equals(type)) {
            // 查询 rider_id = 当前骑手 AND status IN (3,7,8)
            status.add(OrderStatusEnum.ACCEPTED.getCode());
            status.add(OrderStatusEnum.PENDING_PICKUP.getCode());
            status.add(OrderStatusEnum.PICKING_UP.getCode());

            orders = orderMapper.selectList(
                    Wrappers.lambdaQuery(Order.class)
                            .in(Order::getStatus, status)
                            .eq(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                            .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
                            .orderByDesc(Order::getOrderTime)
            );
        }

        if ("delivery".equals(type)) {
            // status = 4
            orders = orderMapper.selectList(
                    Wrappers.lambdaQuery(Order.class)
                            .eq(Order::getStatus, OrderStatusEnum.IN_DELIVERY.getCode())
                            .eq(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                            .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
                            .orderByDesc(Order::getOrderTime)
            );
        }

        if ("completed".equals(type)) {
            // status = 5
            orders = orderMapper.selectList(
                    Wrappers.lambdaQuery(Order.class)
                            .eq(Order::getStatus, OrderStatusEnum.COMPLETED.getCode())
                            .eq(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                            .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
                            .orderByDesc(Order::getOrderTime)
            );
        }


        if (CollUtil.isEmpty(orders)) {
            return List.of();
        }
        List<OrderVO> orderVOS = new ArrayList<>();
        for (Order order : orders) {
            OrderVO orderVO = BeanUtil.toBean(order, OrderVO.class);
            orderVOS.add(orderVO);
        }

        return orderVOS;
    }

    @Override
    public void accept(String orderNo) throws InterruptedException {
        if (orderNo == null) {
            throw new OrderBusinessException(MessageConstant.DO_ERROR);
        }
        RLock lock = redissonClient.getLock("rider_accept_lock:" + orderNo);
        if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
            throw new OrderBusinessException("此订单已被其他骑手接取");
        }
        try {
            List<Integer> status = List.of(OrderStatusEnum.ACCEPTED.getCode(),
                    OrderStatusEnum.PENDING_PICKUP.getCode());
            Order order = orderMapper.selectOne(
                    Wrappers.lambdaQuery(Order.class)
                            .eq(Order::getOrderNo, orderNo)
                            .in(Order::getStatus, status)
            );

            if (order == null) {
                throw new OrderBusinessException(MessageConstant.ORDERS_IS_NULL);
            }

            if (order.getDeliveryEmployeeId() != null) {
                throw new OrderBusinessException(MessageConstant.ORDERS_HAS_BEEN_ACCEPTED);
            }

            order.setDeliveryEmployeeId(BaseContext.getCurrentId());
            int rows = orderMapper.update(
                    null,
                    Wrappers.lambdaUpdate(Order.class)
                            .set(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                            .eq(Order::getOrderNo, orderNo)
                            .isNull(Order::getDeliveryEmployeeId)
            );

            if (rows == 0) {
                throw new OrderBusinessException("此订单已被其他骑手接取");
            }

        } finally {
            lock.unlock();
        }

    }

    @Override
    public OrderMapVO getOrder(String orderNo) {
        Order order = orderMapper.selectOne(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getOrderNo, orderNo)
        );

        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDERS_IS_NULL);
        }

        OrderMapVO vo = BeanUtil.toBean(order, OrderMapVO.class);

        String location = order.getLocation();
        if (StrUtil.isNotBlank(location)) {
            String[] arr = location.split(",");
            vo.setCustomerLongitude(new BigDecimal(arr[0]));
            vo.setCustomerLatitude(new BigDecimal(arr[1]));
        }

        return vo;
    }

    @Override
    public void completed(String orderNo) {
        if (orderNo == null) {
            throw new OrderBusinessException(MessageConstant.DO_ERROR);
        }
        Order order = orderMapper.selectOne(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                        .eq(Order::getStatus, OrderStatusEnum.IN_DELIVERY.getCode())
        );

        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDERS_IS_NULL);
        }

        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setDeliveryTime(LocalDateTime.now());

        orderMapper.updateById(order);
    }

    @Override
    public OrderRiderCountVO countRiderComplete() {
        // 1️⃣ 今天 00:00:00
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // 今日完成单
        Long todayCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                        .eq(Order::getStatus, OrderStatusEnum.COMPLETED.getCode())
                        .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
                        .ge(Order::getDeliveryTime, todayStart)
        );

        Long completedCount = orderMapper.selectCount(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                        .eq(Order::getStatus, OrderStatusEnum.COMPLETED.getCode())
                        .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
        );

        // status: 3, 7
        List<Integer> waitStatus = List.of(
                OrderStatusEnum.ACCEPTED.getCode(),        // 3 待出餐
                OrderStatusEnum.PENDING_PICKUP.getCode()   // 7 待取餐
        );
        Long waitCount = orderMapper.selectCount(
                Wrappers.lambdaQuery(Order.class)
                        .isNull(Order::getDeliveryEmployeeId)
                        .in(Order::getStatus, waitStatus)
                        .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
        );

        // status: 3, 7, 8
        List<Integer> status = List.of(OrderStatusEnum.ACCEPTED.getCode(),
                OrderStatusEnum.PENDING_PICKUP.getCode(),
                OrderStatusEnum.PICKING_UP.getCode());

        Long acceptedCount = orderMapper.selectCount(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
                        .eq(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                        .in(Order::getStatus, status)
        );

        Long deliveryCount = orderMapper.selectCount(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                        .eq(Order::getStatus, OrderStatusEnum.IN_DELIVERY.getCode())
                        .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
        );

        return OrderRiderCountVO.builder()
                .waitCount(waitCount.intValue())
                .deliveryCount(deliveryCount.intValue())
                .completedCount(completedCount.intValue())
                .todayFinished(todayCount.intValue())
                .acceptedCount(acceptedCount.intValue())
                .build();
    }

    @Override
    public OrderRiderTrendVO trend(Integer days) {
        LocalDate now = LocalDate.now();
        // 1️⃣ 今天 00:00:00
        LocalDateTime todayStart = now.atStartOfDay();

        // 2️⃣ 本周一 00:00:00（ISO 标准：周一）
        LocalDateTime weekStart = now
                .with(DayOfWeek.MONDAY)
                .atStartOfDay();

        // 今日完成单
        Long todayCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                        .eq(Order::getStatus, OrderStatusEnum.COMPLETED.getCode())
                        .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
                        .ge(Order::getDeliveryTime, todayStart)
        );

        // 本周完成单
        Long weekCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                        .eq(Order::getStatus, OrderStatusEnum.COMPLETED.getCode())
                        .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
                        .ge(Order::getDeliveryTime, weekStart)
        );

        // 本月完成单
        Long totalCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                        .eq(Order::getStatus, OrderStatusEnum.COMPLETED.getCode())
                        .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
        );
        List<LocalDate> dates = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            dates.add(now.minusDays(i));
        }
        List<String> dateList = dates.stream()
                .map(d -> d.getDayOfMonth() + "日")
                .toList();

        List<Integer> orderCounts = new ArrayList<>();
        List<BigDecimal> incomes = new ArrayList<>();

        for (LocalDate date : dates) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Long count = orderMapper.selectCount(
                    Wrappers.lambdaQuery(Order.class)
                            .eq(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                            .eq(Order::getStatus, OrderStatusEnum.COMPLETED.getCode())
                            .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
                            .ge(Order::getDeliveryTime, beginTime)
                            .le(Order::getDeliveryTime, endTime)
            );
            count = count == null ? 0 : count;

            orderCounts.add(count.intValue());
            BigDecimal income = RiderIncome.multiply(BigDecimal.valueOf(count));
            incomes.add(income);
        }

        return OrderRiderTrendVO.builder()
                .todayFinished(todayCount.intValue())
                .todayIncome(RiderIncome.multiply(BigDecimal.valueOf(todayCount)))
                .weekFinished(weekCount.intValue())
                .weekIncome(RiderIncome.multiply(BigDecimal.valueOf(weekCount)))
                .totalFinished(totalCount.intValue())
                .totalIncome(RiderIncome.multiply(BigDecimal.valueOf(totalCount)))
                .dateList(dateList)
                .orderFinishCountList(orderCounts)
                .incomeList(incomes)
                .build();
    }

    @Override
    public List<RiderOrderStatDTO> getRiderOrders(OrderReportDTO orderReportDTO) {
        LocalDateTime beginTime = LocalDateTime.of(orderReportDTO.getBegin(), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(orderReportDTO.getEnd(), LocalTime.MAX);

        return orderMapper.selectRiderOrderStat(beginTime, endTime);
    }

    @Override
    public List<RiderSalaryStatDTO> getRiderSalary(OrderReportDTO orderReportDTO) {
        LocalDateTime beginTime = LocalDateTime.of(orderReportDTO.getBegin(), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(orderReportDTO.getEnd(), LocalTime.MAX);

        return orderMapper.selectRiderSalaryStat(beginTime, endTime);
    }

    @Override
    public void foodCompleted(Long id) {
        if (id == null) {
            throw new OrderBusinessException(MessageConstant.DO_ERROR);
        }

        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDERS_IS_NULL);
        }

        if (order.getOrderType().equals(OrderTypeEnum.DELIVERY.getCode())) {
            int row = 0;
            // 外卖单
            Integer status = order.getStatus();
            // 骑手未点击取餐 商家点击出餐 状态为待取餐
            if (status.equals(OrderStatusEnum.ACCEPTED.getCode())) {
                order.setStatus(OrderStatusEnum.PENDING_PICKUP.getCode());
                row = orderMapper.updateById(order);

            } else if (status.equals(OrderStatusEnum.PICKING_UP.getCode())) {
                // 骑手已点击取餐 商家点击出餐 直接为派送中
                order.setStatus(OrderStatusEnum.IN_DELIVERY.getCode());
                row = orderMapper.updateById(order);
            }

            if (row < 1) {
                throw new OrderBusinessException(MessageConstant.UPDATE_ERROR);
            }
        } else {
            // 堂食单
            order.setStatus(OrderStatusEnum.COMPLETED.getCode());
            int row = orderMapper.updateById(order);

            if (row < 1) {
                throw new OrderBusinessException(MessageConstant.UPDATE_ERROR);
            }
        }
    }

    @Override
    public Integer pickUp(String orderNo) {
        if (StrUtil.isBlank(orderNo)) {
            throw new OrderBusinessException(MessageConstant.DO_ERROR);
        }

        Order order = orderMapper.selectOne(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getDeliveryEmployeeId, BaseContext.getCurrentId())
                        .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
        );

        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDERS_IS_NULL);
        }

        Integer status = order.getStatus();
        if (!(Objects.equals(status, OrderStatusEnum.ACCEPTED.getCode()) ||
                Objects.equals(status, OrderStatusEnum.PENDING_PICKUP.getCode()))) {

            throw new OrderBusinessException("当前状态不可取餐");
        }

        if (status.equals(OrderStatusEnum.ACCEPTED.getCode())) {
            // 待出餐
            status = OrderStatusEnum.PICKING_UP.getCode();
            order.setStatus(status);

        } else {
            // 待取餐
            status = OrderStatusEnum.IN_DELIVERY.getCode();
            order.setStatus(status);
        }

        int row = orderMapper.updateById(order);

        if (row < 1) {
            throw new OrderBusinessException(MessageConstant.UPDATE_ERROR);
        }

        return status;
    }


}
