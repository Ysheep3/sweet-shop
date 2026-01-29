package com.sweet.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.response.AlipayTradeCreateResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sweet.api.client.CartClient;
import com.sweet.api.client.CouponClient;
import com.sweet.api.client.UserClient;
import com.sweet.api.client.UserCouponClient;
import com.sweet.api.dto.*;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.AddressBookBusinessException;
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
import com.sweet.order.entity.dto.OrderDTO;
import com.sweet.order.entity.dto.OrderPayDTO;
import com.sweet.order.entity.dto.OrdersPageDTO;
import com.sweet.order.entity.dto.UserCouponDTO;
import com.sweet.order.entity.pojo.Order;
import com.sweet.order.entity.pojo.OrderDetail;
import com.sweet.order.entity.vo.*;
import com.sweet.order.mapper.OrderDetailMapper;
import com.sweet.order.mapper.OrderMapper;
import com.sweet.order.service.OrderService;
import com.sweet.order.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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
    private final AmapProperties amapProperties;
    private final ShopProperties shopProperties;
    private final RedissonClient redissonClient;

    // 配送费 3元
    private static final BigDecimal ShippingFees = BigDecimal.valueOf(3);
    // 假设骑手每单收入1元
    private static final BigDecimal RiderIncome = BigDecimal.valueOf(2);


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
            RBucket<String> bucket = redissonClient.getBucket("shop_location");

            String shopLocation = bucket.get();

            if (StrUtil.isBlank(shopLocation)) {
                shopLocation = getLocation(shopAddress); // 调高德 geocode
                bucket.set(shopLocation);
            }

            // 计算距离 大于5000则抛异常
            calculateDistance(userLocation, shopLocation);
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

    private void calculateDistance(String origin, String destination) {
        try {
            String url = "https://restapi.amap.com/v3/distance";

            Map<String, String> params = new HashMap<>();
            params.put("key", amapProperties.getKey());
            params.put("origins", origin);        // lng,lat
            params.put("destination", destination);
            params.put("type", "0");              // 直线距离

            String result = HttpClientUtil.doGet(url, params);
            JSONObject json = JSONUtil.parseObj(result);

            JSONArray results = json.getJSONArray("results");
            if (CollUtil.isEmpty(results)) {
                throw new OrderBusinessException("距离计算失败");
            }

            Integer distance = results.getJSONObject(0).getInt("distance");

            if (distance > 5000) {
                throw new OrderBusinessException(MessageConstant.ORDERS_DISTANCE_ERROR);
            }
        } catch (Exception e) {
            log.error("高德距离计算异常", e);
            throw new OrderBusinessException("距离计算失败");
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

        return OrderCountVO.builder()
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
    public void confirm(OrderDTO orderDTO) {
        Order order = getById(orderDTO);
        order.setStatus(OrderStatusEnum.ACCEPTED.getCode());

        orderMapper.updateById(order);
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

        // 前端json格式接收
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);
        map.put("orderId", id);
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
    public List<OrderVO> getByStatus(Integer status) {
        if (status == null) {
            throw new OrderBusinessException(MessageConstant.DO_ERROR);
        }

        List<Order> orders = orderMapper.selectList(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getStatus, status)
                        .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
                        .orderByDesc(Order::getOrderTime)
        );

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
    public void accept(String orderNo) {
        if (orderNo == null) {
            throw new OrderBusinessException(MessageConstant.DO_ERROR);
        }
        Order order = orderMapper.selectOne(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getStatus, OrderStatusEnum.ACCEPTED.getCode())
        );

        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDERS_IS_NULL);
        }

        if (order.getDeliveryEmployeeId() != null) {
            throw new OrderBusinessException(MessageConstant.ORDERS_HAS_BEEN_ACCEPTED);
        }

        order.setStatus(OrderStatusEnum.IN_DELIVERY.getCode());
        order.setDeliveryEmployeeId(BaseContext.getCurrentId());
        orderMapper.updateById(order);
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

        Long waitCount = orderMapper.selectCount(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getStatus, OrderStatusEnum.ACCEPTED.getCode())
                        .eq(Order::getOrderType, OrderTypeEnum.DELIVERY.getCode())
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
}