package com.sweet.user.controller;

import com.sweet.api.dto.OrderReportDTO;
import com.sweet.api.dto.OrderReportVO;
import com.sweet.api.dto.SalesTop10ReportVO;
import com.sweet.api.dto.TurnoverReportVO;
import com.sweet.common.result.Result;
import com.sweet.user.entity.vo.UserReportVO;
import com.sweet.user.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(OrderReportDTO orderReportDTO) {
        OrderReportVO orderReportVO = reportService.getOrdersStatistics(orderReportDTO);
        return Result.success(orderReportVO);
    }

    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(OrderReportDTO orderReportDTO) {
        TurnoverReportVO turnoverReportVO = reportService.getTurnoverStatistics(orderReportDTO);
        return Result.success(turnoverReportVO);
    }

    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(OrderReportDTO orderReportDTO) {
        UserReportVO userReportVO = reportService.getUserStatistics(orderReportDTO);
        return Result.success(userReportVO);
    }

    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(OrderReportDTO orderReportDTO) {
        SalesTop10ReportVO salesTop10ReportVO = reportService.getTop10(orderReportDTO);
        return Result.success(salesTop10ReportVO);
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        reportService.getExcel(response);
    }
}
