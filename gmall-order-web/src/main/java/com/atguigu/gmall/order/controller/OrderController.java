package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.bean.enums.PaymentWay;
import com.atguigu.gmall.manage.util.Constant;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author gaochen
 * @create 2019-02-12 10:52
 */
@Controller
public class OrderController {

    @Reference
    private CartService cartService;
    @Reference
    private UserInfoService userInfoService;
    @Reference
    private OrderService orderService;
    @Reference
    private SkuService skuService;

    @LoginRequired(isNeedLogin = true)
    @RequestMapping("submitOrder")
    public String submitOrder(String deliveryAddressId, String tradeCode, HttpServletRequest request,
                              HttpServletResponse response, ModelMap map) {
        String userId = (String) request.getAttribute("userId");
        boolean b = orderService.checkTradeCode(tradeCode, userId);
        if (b) {
            UserAddress userAddress = userInfoService.getAddressById(deliveryAddressId);
            List<CartInfo> cartInfos = cartService.cartListFromCache(userId);
            // 订单保存业务(数据的一致性校验，库存价格)
            // 对订单对象进行封装
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setProcessStatus(Constant.ORDER_COMMIT);
            orderInfo.setOrderStatus(Constant.ORDER_COMMIT);
            String outTradeNo = "atguigu" + userId;
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = sdf.format(date);
            outTradeNo = outTradeNo + format + System.currentTimeMillis();
            orderInfo.setOutTradeNo(outTradeNo);
            orderInfo.setUserId(userId);
            orderInfo.setPaymentWay(PaymentWay.ONLINE);
            BigDecimal mySum = getMySum(cartInfos);
            orderInfo.setTotalAmount(mySum);
            orderInfo.setOrderComment(Constant.ORDER_COMMENT);
            orderInfo.setDeliveryAddress(userAddress.getUserAddress());
            orderInfo.setCreateTime(new Date());
            orderInfo.setConsignee(userAddress.getConsignee());
            orderInfo.setConsigneeTel(userAddress.getPhoneNum());
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 1);
            orderInfo.setExpireTime(c.getTime());
            List<OrderDetail> orderDetails = new ArrayList<>();
            List<String> delCartIds = new ArrayList<>();
            // 对订单详情进行封装
            for (CartInfo cartInfo : cartInfos) {
                if (cartInfo.getIsChecked().equals("1")) {
                    // 查验价格
                    SkuInfo sku = skuService.getSkuById(cartInfo.getSkuId());
                    int i = sku.getPrice().compareTo(cartInfo.getSkuPrice());
                    if (i == 0) {
                        OrderDetail orderDetail = new OrderDetail();
                        orderDetail.setSkuNum(cartInfo.getSkuNum());
                        orderDetail.setImgUrl(cartInfo.getImgUrl());
                        orderDetail.setOrderPrice(cartInfo.getCartPrice());
                        orderDetail.setSkuId(cartInfo.getSkuId());
                        orderDetail.setSkuNum(cartInfo.getSkuNum());
                        orderDetails.add(orderDetail);
                        delCartIds.add(cartInfo.getId());
                    } else {
                        return "tradeFail";
                    }
                }
            }
            orderInfo.setOrderDetailList(orderDetails);
            orderService.saveOrder(orderInfo);
            // 重定向支付系统，由支付系统对接支付宝平台，完成支付业务
            return "redirect:http://payment.gmall.com:8090/paymentIndex?outTradeNo=" + outTradeNo + "&totalAmount=" + mySum;
        } else {
            return "tradeFail";
        }
    }

    @LoginRequired(isNeedLogin = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, HttpServletResponse response, ModelMap map) {
        String userId = (String) request.getAttribute("userId");
        // 根据userId查询缓存中的购物车数据
        List<CartInfo> cartInfos = cartService.cartListFromCache(userId);
        // 将购物车数据转化为订单列表数据
        List<Object> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfos) {
            if (cartInfo.getIsChecked().equals("1")) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setHasStock("1");
                orderDetailList.add(orderDetail);
            }
        }
        // 查询userId的 收货人地址
        List<UserAddress>  userAddressList = userInfoService.getAddressListByUserId(userId);
        map.put("userAddressList", userAddressList);
        map.put("orderDetailList",orderDetailList);
        map.put("totalAmount",getMySum(cartInfos));
        // 生成 交易码，写入缓存
        String tradeCode = UUID.randomUUID().toString();
        map.put("tradeCode", tradeCode);
        orderService.genTradeCode(tradeCode,userId);
        return "trade";
    }

    private BigDecimal getMySum(List<CartInfo> cartInfos) {
        BigDecimal bigDecimal = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfos) {
            String isChecked = cartInfo.getIsChecked();
            if(isChecked.equals("1")) {
                bigDecimal = bigDecimal.add(cartInfo.getCartPrice());
            }
        }
        return bigDecimal;
    }
}
