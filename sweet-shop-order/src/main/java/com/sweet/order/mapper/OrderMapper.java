package com.sweet.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sweet.api.dto.OrderOverViewVO;
import com.sweet.order.entity.dto.OrdersPageDTO;
import com.sweet.order.entity.pojo.Order;
import com.sweet.order.entity.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    IPage<OrderVO> pageQuery(Page<OrderVO> page, @Param("order") OrdersPageDTO ordersPageDTO);

    OrderOverViewVO countOrderByStatus(LocalDateTime beginTime, LocalDateTime endTime);
}
