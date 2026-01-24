package com.sweet.order.controller.notify;

import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.common.context.BaseContext;
import com.sweet.common.properties.AlipayProperties;
import com.sweet.order.common.OrderPayStatusEnum;
import com.sweet.order.common.OrderStatusEnum;
import com.sweet.order.entity.pojo.Order;
import com.sweet.order.mapper.OrderMapper;
import com.sweet.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.sweet.order.common.OrderPayStatusEnum.PAID;

@RestController
@RequiredArgsConstructor
public class PayNotifyController {
    private final OrderMapper orderMapper;
    private final AlipayProperties alipayProperties;

    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) throws Exception {
        // ===== 1. 取所有参数 =====
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            params.put(name, requestParams.get(name)[0]);
        }

        // ===== 2. 验签（最重要）=====
        boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getCharset(),
                alipayProperties.getSignType()
        );

        if (!signVerified) {
            return "failure";
        }

        // ===== 3. 关键参数 =====
        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String totalAmount = params.get("total_amount");

        // ===== 4. 判断支付成功 =====
        if ("TRADE_SUCCESS".equals(tradeStatus)
                || "TRADE_FINISHED".equals(tradeStatus)) {

            // ===== 5. TODO：幂等处理（非常重要）=====
            Order order = orderMapper.selectOne(
                    Wrappers.lambdaQuery(Order.class)
                            .eq(Order::getUserId, BaseContext.getCurrentId())
                            .eq(Order::getOrderNo, outTradeNo)
            );
            if (Objects.equals(order.getStatus(), OrderStatusEnum.PENDING_ACCEPTANCE.getCode())
                    || order.getPayStatus().equals(OrderPayStatusEnum.PAID.getCode()))
                return "success";

            // ===== 6. 更新订单状态 =====
            order.setStatus(OrderStatusEnum.PENDING_ACCEPTANCE.getCode());
            order.setPayStatus(OrderPayStatusEnum.PAID.getCode());
            order.setPayTime(LocalDateTime.now());
            order.setPayTime(LocalDateTime.now());
            order.setPayMethod(0);
            orderMapper.updateById(order);

            System.out.println("订单支付成功：" + outTradeNo);
        }

        // ===== 7. 必须返回 success =====
        return "success";
    }

//@GetMapping("/return")
//public void getReturn(HttpServletRequest request, HttpServletResponse response) throws Exception {
//    Map<String, String> params = new HashMap<>();
//    Map<String, String[]> requestParams = request.getParameterMap();
//    for (String name : requestParams.keySet()) {
//        params.put(name, request.getParameter(name));
//    }
//
//    // 支付宝验签
//    if (Factory.Payment.Common().verifyNotify(params)) {
//        // 验签通过
//
//        response.sendRedirect("http://8.148.225.176/#/success");
//    }
//}

}
