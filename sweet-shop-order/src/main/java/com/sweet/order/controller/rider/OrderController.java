package com.sweet.order.controller.rider;

import com.sweet.common.result.Result;
import com.sweet.order.entity.dto.OrderDTO;
import com.sweet.order.entity.dto.OrderPayDTO;
import com.sweet.order.entity.vo.*;
import com.sweet.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("riderOrderController")
@RequestMapping("/order/rider")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{type}")
    public Result<List<OrderVO>> listByStatus(@PathVariable String type) {
        List<OrderVO> orderVOS = orderService.getByStatus(type);
        return Result.success(orderVOS);
    }

    @GetMapping("/getByOrderNo/{orderNo}")
    public Result<OrderMapVO> getByOrderNo(@PathVariable String orderNo) {
        OrderMapVO vo = orderService.getOrder(orderNo);
        return Result.success(vo);
    }

    @PutMapping("/accept/{orderNo}")
    public Result<Void> acceptOrder(@PathVariable String orderNo) throws InterruptedException {
        orderService.accept(orderNo);
        return Result.success();
    }

    @PutMapping("/completed/{orderNo}")
    public Result<Void> completed(@PathVariable String orderNo) {
        orderService.completed(orderNo);
        return Result.success();
    }

    @GetMapping
    public Result<OrderRiderCountVO> countRiderComplete() {
        OrderRiderCountVO vo = orderService.countRiderComplete();
        return Result.success(vo);
    }

    @GetMapping("/trend/{days}")
    public Result<OrderRiderTrendVO> trend(@PathVariable Integer days) {
        OrderRiderTrendVO vo = orderService.trend(days);
        return Result.success(vo)   ;
    }

    @PutMapping("/pickUp/{orderNo}")
    public Result<Integer> pickUp(@PathVariable String orderNo) {
        Integer status = orderService.pickUp(orderNo);
        return Result.success(status);
    }
}
