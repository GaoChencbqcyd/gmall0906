package com.atguigu.gmall.order.orderListener;


import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderMqListener {

    @Autowired
    private OrderService orderService;

    @JmsListener(containerFactory = "jmsQueueListener" ,destination = "PAYMENT_SUCCESS_QUEUE")
    public void consumePaymentResult(MapMessage mapMessage){
        String out_trade_no = null;
        String tracking_no = null;
        String payment_status = null;
        try {
            out_trade_no = mapMessage.getString("out_trade_no");
            tracking_no = mapMessage.getString("tracking_no");
            payment_status = mapMessage.getString("payment_status");
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.out.println(out_trade_no+"已经完成，请指示");

        // 消费代码
        orderService.updateOrder(out_trade_no,payment_status,tracking_no);

        // 发送一个订单成功的消息队列，由库存系统消费(或者调用库存接口)
        orderService.sendOrderResult(out_trade_no);
    }

}
