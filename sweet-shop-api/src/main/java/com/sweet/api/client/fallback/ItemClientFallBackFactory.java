//package com.sweet.api.client.fallback;
//
//import com.hmall.client.ItemClient;
//import com.hmall.common.utils.CollUtils;
//import com.hmall.dto.ItemDTO;
//import com.hmall.dto.OrderDetailDTO;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.openfeign.FallbackFactory;
//
//import java.util.Collection;
//import java.util.List;
//

// 处理微服务调用失败时的兜底逻辑

//@Slf4j
//public class ItemClientFallBackFactory implements FallbackFactory<ItemClient> {
//    @Override
//    public ItemClient create(Throwable cause) {   // cause: 错误信息
//        return new ItemClient() {
//            @Override
//            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
//                log.error("查询购物车失败: ", cause);
//                return CollUtils.emptyList();
//            }
//            @Override
//            public void deductStock(List<OrderDetailDTO> items) {
//                log.error("减少库存失败:", cause);
//                throw new RuntimeException(cause);
//            }
//        };
//    }
//}
