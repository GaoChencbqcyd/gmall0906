package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.manage.util.Constant;
import com.atguigu.gmall.payment.conf.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gaochen
 * @create 2019-02-12 19:04
 */
@Controller
public class PaymentController {

    @Autowired
    private AlipayClient alipayClient;

    @Reference
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @RequestMapping("/alipay/callback/return")
    public String callBackReturn(HttpServletRequest request, Map<String,String> paramsMap) {
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_no = request.getParameter("trade_no");
        String sign = request.getParameter("sign");
        try {
            // 对支付宝回调签名的校验
            boolean b = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key,AlipayConfig.charset,
                    AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        // 修改支付信息，幂等性检查
        boolean b = paymentService.checkPaymentStatus(out_trade_no);
        if(!b) {
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus(Constant.ORDER_PAYMENT);
            paymentInfo.setCallbackContent(request.getQueryString());
            paymentInfo.setOutTradeNo(out_trade_no);
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackTime(new Date());
            paymentService.updatePayment(paymentInfo);
            // 发送系统消息，出发并发商品支付业务服务消息队列
            paymentService.sendPaymentSuccess(paymentInfo.getOutTradeNo(),paymentInfo.getPaymentStatus(),trade_no);
        }
        return "finish";
    }

    @LoginRequired(isNeedLogin = true)
    @RequestMapping("/alipay/submit")
    @ResponseBody
    public String goToPay(HttpServletRequest request, String outTradeNo , BigDecimal totalAmount, ModelMap map) {
       OrderInfo orderInfo = orderService.getOrderByOutTradeNo(outTradeNo);
        String skuName = orderInfo.getOrderDetailList().get(0).getSkuName();
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        //在公共参数中设置回跳和通知地址
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("out_trade_no", outTradeNo);
        requestMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        requestMap.put("total_amount","0.01");
        requestMap.put("subject",skuName);

        alipayRequest.setBizContent(JSON.toJSONString(requestMap));
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        // 生成(保存)支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setSubject(skuName);
        paymentInfo.setCreateTime(new Date());
        paymentService.save(paymentInfo);
        // 发送检查支付结果的消息队列
        paymentService.sendDelayPaymentCheck(outTradeNo,5);
        return form;
    }

    @LoginRequired(isNeedLogin = true)
    @RequestMapping("paymentIndex")
    public String paymentIndex(HttpServletRequest request, String outTradeNo , BigDecimal totalAmount, ModelMap map) {
        String userId = (String)request.getAttribute("userId");
        map.put("outTradeNo",outTradeNo);
        map.put("totalAmount",totalAmount);
        return "paymentindex";
    }
}
