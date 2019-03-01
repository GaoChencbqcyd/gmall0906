package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo; /**
 * @author gaochen
 * @create 2019-02-12 19:07
 */
public interface PaymentService {
    void updatePayment(PaymentInfo paymentInfo);

    void save(PaymentInfo paymentInfo);

    boolean checkPaymentStatus(String out_trade_no);

    void sendPaymentSuccess(String outTradeNo, String paymentStatus, String trade_no);

    void sendDelayPaymentCheck(String outTradeNo, int i);

    PaymentInfo checkPaymentResult(String out_trade_no);
}
