package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单的方法
     */
    @Scheduled(cron = "0 * * * * ? ")   // 每分钟触发一次
//    @Scheduled(cron = "1/5 * * * * ?")  // 每5s触发一次 从第一秒开始（测试）
    public void processTimeOutOrder() {
        log.info("定时处理超时订单: {}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-5);
        List<Orders> orderList = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, time);
        if(orderList != null && orderList.size() > 0) {
            for(Orders order : orderList) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时 自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }

    /**
     * 处理一致处于派送中状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ? ")    // 每天凌晨一点触发一次
//    @Scheduled(cron = "0/5 * * * * ?")  // 每5s触发一次（测试）
    public void processDeliveryOrder() {
        log.info("处理一致处于派送中状态的订单: {}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusHours(-1);
        List<Orders> orderList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, time);
        if(orderList != null && orderList.size() > 0) {
            for(Orders order : orderList) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
