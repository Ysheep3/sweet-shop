package com.sweet.order.controller.user;

import com.sweet.common.result.Result;
import com.sweet.order.entity.dto.OrderDTO;
import com.sweet.order.entity.dto.OrderPayDTO;
import com.sweet.order.entity.vo.OrderPayVO;
import com.sweet.order.entity.vo.OrderVO;
import com.sweet.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.Map;

@RestController("userOrderController")
@RequestMapping("/order/user")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/detail/{orderNo}")
    public Result<OrderVO> getByOrderNo(@PathVariable String orderNo) {
        OrderVO vo = orderService.getByOrderNo(orderNo);
        return Result.success(vo);
    }

    @PostMapping("/submit")
    public Result<OrderPayVO> createOrder(@RequestBody OrderDTO requestParam) {
        OrderPayVO orderPayVO = orderService.createOrder(requestParam);
        return Result.success(orderPayVO);
    }

    @PostMapping("/listByStatus")
    public Result<List<OrderVO>> listByStatus(@RequestBody List<Integer> statusList) {
        List<OrderVO> orderVOS = orderService.listByStatus(statusList);
        return Result.success(orderVOS);
    }

    @PostMapping("/pay")
    public Result<Void> pay(@RequestBody OrderPayDTO requestParam) {
        orderService.pay(requestParam);
        return Result.success();
    }

    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success();
    }

    @PutMapping("/completed/{id}")
    public Result<Void> completed(@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }

    @PostMapping("/again/{id}")
    public Result<Void> again(@PathVariable Long id) {
        orderService.again(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    public Result<Void> reminder(@PathVariable Long id) {
        orderService.reminder(id);
        return Result.success();
    }
}
