package com.sweet.order.service;

import com.sweet.api.dto.*;
import com.sweet.common.result.PageResult;
import com.sweet.order.entity.dto.OrderDTO;
import com.sweet.order.entity.dto.OrderPayDTO;
import com.sweet.order.entity.dto.OrdersPageDTO;
import com.sweet.order.entity.vo.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService {
    /**
     * 用户提交订单
     *
     */
    OrderPayVO createOrder(OrderDTO requestParam);

    List<OrderVO> listByStatus(List<Integer> statusList);

    void pay(OrderPayDTO requestParam);

    /**
     * 统计各个状态下的订单数量
     *
     * @return
     */
    OrderCountVO countByStatus(Long userId);

    /**
     * 根据id查询订单详情
     *
     * @param id
     * @return
     */
    OrderVO getOrderWithDetailsById(Long id);

    /**
     * 订单搜索
     *
     * @param ordersPageDTO
     * @return
     */
    PageResult pageQuery(OrdersPageDTO ordersPageDTO);

    /**
     * 接单
     * @param orderDTO
     */
    void confirm(OrderDTO orderDTO);

    /**
     * 拒单
     * @param orderDTO
     */
    void rejection(OrderDTO orderDTO);

    /**
     * 管理员 取消订单
     * @param orderDTO
     */
    void cancel(OrderDTO orderDTO);

    /**
     * 用户 取消订单
     *
     * @param id
     */
    void cancel(Long id);
    /**
     * 用户催单
     *
     * @param id
     */
    void reminder(Long id);

    /**
     * 再来一单
     *
     * @param id
     */
    void again(Long id);

    /**
     * 派单
     * @param id
     */
//    void delivery(Long id);

    /**
     * 确认订单
     * @param id
     */
    void complete(Long id);

    /**
     * 获取 营业数据
     * @param beginTime
     * @param endTime
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 查询订单管理数据
     *
     * @return
     */
    OrderOverViewVO getOverviewOrders();

    /**
     * 统计
     * @param orderReportDTO
     * @return
     */
    OrderReportVO getOrdersStatistics(OrderReportDTO orderReportDTO);

    TurnoverReportVO getTurnoverStatistics(OrderReportDTO orderReportDTO);

    SalesTop10ReportVO getTop10(OrderReportDTO orderReportDTO);

    OrderVO getByOrderNo(String orderNo);

    /**
     * 骑手获取订单
     * @param status
     * @return
     */
    List<OrderVO> getByStatus(Integer status);

    /**
     * 骑手接单
     * @param orderNo
     */
    void accept(String orderNo);

    OrderMapVO getOrder(String orderNo);

    /**
     * 骑手已送达
     *
     * @param orderNo
     */
    void completed(String orderNo);

    /**
     * 统计骑手完成订单数
     * @return
     */
    OrderRiderCountVO countRiderComplete();

    /**
     * 统计骑手数据
     * @param days
     * @return
     */
    OrderRiderTrendVO trend(Integer days);
}
