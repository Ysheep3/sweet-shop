package com.sweet.user.service;

import com.sweet.api.dto.*;
import com.sweet.user.entity.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;

public interface ReportService {
    /**
     * 订单统计
     *
     * @param orderReportDTO
     * @return
     */
    OrderReportVO getOrdersStatistics(OrderReportDTO orderReportDTO);

    /**
     * 营业额统计
     *
     * @param orderReportDTO
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(OrderReportDTO orderReportDTO);

    /**
     * 用户量统计
     *
     * @param orderReportDTO
     * @return
     */
    UserReportVO getUserStatistics(OrderReportDTO orderReportDTO);

    /**
     * 查询销量前十
     *
     * @param orderReportDTO
     * @return
     */
    SalesTop10ReportVO getTop10(OrderReportDTO orderReportDTO);

    /**
     * 导出Excel表 (近 30天 的数据)
     *
     * @param response
     */
    void getExcel(HttpServletResponse response);

    /**
     * 统计 骑手订单数据
     * @param orderReportDTO
     * @return
     */
    RiderOrdersReportVO getRiderOrders(OrderReportDTO orderReportDTO);

    /**
     * 统计 骑手薪资数据
     * @param orderReportDTO
     * @return
     */
    RiderSalaryReportVO getRiderSalary(OrderReportDTO orderReportDTO);
}
