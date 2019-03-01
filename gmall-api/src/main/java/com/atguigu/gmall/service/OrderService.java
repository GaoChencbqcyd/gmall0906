package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo; /**
 * @author gaochen
 * @create 2019-02-12 10:39
 */
public interface OrderService {
    boolean checkTradeCode(String tradeCode, String userId);

    void saveOrder(OrderInfo orderInfo);

    void genTradeCode(String tradeCode, String userId);

    OrderInfo getOrderByOutTradeNo(String outTradeNo);

    void updateOrder(String out_trade_no, String payment_status, String tracking_no);

    void sendOrderResult(String out_trade_no);
}
