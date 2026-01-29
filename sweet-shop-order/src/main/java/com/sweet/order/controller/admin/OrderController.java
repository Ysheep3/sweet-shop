package com.sweet.order.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.sweet.api.dto.*;
import com.sweet.common.result.PageResult;
import com.sweet.common.result.Result;
import com.sweet.order.entity.dto.OrderDTO;
import com.sweet.order.entity.dto.OrdersPageDTO;
import com.sweet.order.entity.pojo.Order;
import com.sweet.order.entity.vo.OrderCountVO;
import com.sweet.order.entity.vo.OrderVO;
import com.sweet.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author Ysheep
 * @since 2024-11-19
 */
@RestController("adminOrderController")
@RequestMapping("/order/admin")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;


    @GetMapping("/conditionSearch")
    public Result<PageResult> page(OrdersPageDTO ordersPageDTO) {
        PageResult pageResult = orderService.pageQuery(ordersPageDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/details/{id}")
    public Result<OrderVO> getById(@PathVariable Long id) {
        OrderVO orderVO = orderService.getOrderWithDetailsById(id);
        return Result.success(orderVO);
    }

    @GetMapping("/statistics")
    public Result<OrderCountVO> count() {
        OrderCountVO countVO = orderService.countByStatus(null);
        return Result.success(countVO);
    }

    @PutMapping("/confirm")
    public Result<Void> confirm(@RequestBody OrderDTO orderDTO) {
        orderService.confirm(orderDTO);
        return Result.success();
    }

    @PutMapping("/rejection")
    public Result<Void> rejection(@RequestBody OrderDTO orderDTO) {
        orderService.rejection(orderDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    public Result<Void> cancel(@RequestBody OrderDTO orderDTO) {
        orderService.cancel(orderDTO);
        return Result.success();
    }

//    @PutMapping("/delivery/{id}")
//    public Result<Void> delivery(@PathVariable Long id) {
//        orderService.delivery(id);
//        return Result.success();
//    }

    @PutMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }

    @GetMapping("/businessData")
    Result<BusinessDataVO> getBusinessData(LocalDateTime beginTime, LocalDateTime endTime) {
        BusinessDataVO vo = orderService.getBusinessData(beginTime, endTime);
        return Result.success(vo);
    }

    @GetMapping("/overviewOrders")
    Result<OrderOverViewVO> overviewOrders() {
        OrderOverViewVO vo = orderService.getOverviewOrders();
        return Result.success(vo);
    }

    @PostMapping("/ordersStatistics")
    Result<OrderReportVO> ordersStatistics(@RequestBody OrderReportDTO orderReportDTO) {
        OrderReportVO vo = orderService.getOrdersStatistics(orderReportDTO);
        return Result.success(vo);
    }

    @PostMapping("/turnoverStatistics")
    Result<TurnoverReportVO> turnoverStatistics(@RequestBody OrderReportDTO orderReportDTO) {
        TurnoverReportVO vo = orderService.getTurnoverStatistics(orderReportDTO);
        return Result.success(vo);
    }

    @PostMapping("/top10")
    Result<SalesTop10ReportVO> top10(@RequestBody OrderReportDTO orderReportDTO) {
        SalesTop10ReportVO vo = orderService.getTop10(orderReportDTO);
        return Result.success(vo);
    }
}
