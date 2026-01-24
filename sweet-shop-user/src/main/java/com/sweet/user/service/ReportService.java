package com.sweet.user.service;

import com.sweet.api.dto.OrderReportDTO;
import com.sweet.api.dto.OrderReportVO;
import com.sweet.api.dto.SalesTop10ReportVO;
import com.sweet.api.dto.TurnoverReportVO;
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
}
