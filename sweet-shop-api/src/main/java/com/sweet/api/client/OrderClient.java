package com.sweet.api.client;

import com.sweet.api.dto.*;
import com.sweet.common.result.Result;
import org.apache.catalina.LifecycleState;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(value = "sweet-shop-order", path = "/order")
public interface OrderClient {

    @GetMapping("/admin/businessData")
    Result<BusinessDataVO> getBusinessData(@RequestParam LocalDateTime beginTime,
                                           @RequestParam LocalDateTime endTime);

    @GetMapping("/admin/overviewOrders")
    Result<OrderOverViewVO> overviewOrders();

    @PostMapping("/admin/ordersStatistics")
    Result<OrderReportVO> ordersStatistics(@RequestBody OrderReportDTO orderReportDTO);

    @PostMapping("/admin/turnoverStatistics")
    Result<TurnoverReportVO> turnoverStatistics(@RequestBody OrderReportDTO orderReportDTO);

    @PostMapping("/admin/top10")
    Result<SalesTop10ReportVO> top10(@RequestBody OrderReportDTO orderReportDTO);

    @PostMapping("/admin/riderOrders")
    Result<List<RiderOrderStatDTO>> getRiderOrders(@RequestBody OrderReportDTO orderReportDTO);

    @PostMapping("/admin/riderSalary")
    Result<List<RiderSalaryStatDTO>> getRiderSalary(@RequestBody OrderReportDTO orderReportDTO);
}
