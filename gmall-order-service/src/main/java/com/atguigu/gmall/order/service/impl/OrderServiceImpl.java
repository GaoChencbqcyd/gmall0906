package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.PaymentWay;
import com.atguigu.gmall.conf.ActiveMQUtil;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;

/**
 * @author gaochen
 * @create 2019-02-12 10:38
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Override
    public boolean checkTradeCode(String tradeCode, String userId) {
        boolean b = false;
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeFromCache = jedis.get("user:" + userId + ":tradeCode");
        if(tradeCode.equals(tradeCodeFromCache)) {
            b = true;
            jedis.del("user:" + userId + ":tradeCode");
        }
        return b;
    }

    @Override
    public void saveOrder(OrderInfo orderInfo) {
        orderInfoMapper.insertSelective(orderInfo);
        String id = orderInfo.getId();
        // 保存订单详情
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(id);
            orderDetailMapper.insertSelective(orderDetail);
        }
    }

    @Override
    public void genTradeCode(String tradeCode, String userId) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:" + userId + ":tradeCode",60*30, tradeCode);
        jedis.close();
    }

    @Override
    public OrderInfo getOrderByOutTradeNo(String outTradeNo) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo = orderInfoMapper.selectOne(orderInfo);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfo.getId());
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(orderDetails);
        return orderInfo;
    }

    @Override
    public void updateOrder(String out_trade_no, String payment_status, String tracking_no) {

        // 更新订单状态 订单状态、支付方式、交易状态、支付宝交易号

        Example e = new Example(OrderInfo.class);
        e.createCriteria().andEqualTo("outTradeNo",out_trade_no);

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus("订单已支付");
        orderInfo.setPaymentWay(PaymentWay.ONLINE);
        orderInfo.setProcessStatus("订单已支付");
        orderInfo.setTrackingNo(tracking_no);

        orderInfoMapper.updateByExampleSelective(orderInfo,e);
    }

    @Override
    public void sendOrderResult(String out_trade_no) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(out_trade_no);
        OrderInfo order = orderInfoMapper.selectOne(orderInfo);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(order.getId());
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);

        order.setOrderDetailList(orderDetails);

        try {
            // 连接消息服务器
            ConnectionFactory connect = activeMQUtil.getConnectionFactory();
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            // 发送消息
            Queue testqueue = session.createQueue("ORDER_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(testqueue);
            TextMessage textMessage=new ActiveMQTextMessage();

            textMessage.setText(JSON.toJSONString(order));

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();// 事务型消息，必须提交后才生效
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
