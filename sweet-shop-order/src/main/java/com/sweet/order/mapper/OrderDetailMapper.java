package com.sweet.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sweet.order.entity.pojo.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
    void insertBatchs(List<OrderDetail> orderDetails);

    List<OrderDetail> getTop10(List<Long> orderIds);
}
