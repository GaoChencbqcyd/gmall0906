package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-23 11:45
 */
@Controller
public class CartController {
    @Reference
    private SkuService skuService;

    @Reference
    private CartService cartService;

//    @LoginRequired(isNeedLogin = true)
//    @RequestMapping("toTrade")
//    public String toTrade(HttpServletRequest request, HttpServletResponse response, ModelMap map) {
//        String userId = (String) request.getAttribute("userId");
//        return userId;
//    }

    @LoginRequired(isNeedLogin = false)
    @RequestMapping("checkCart")
    public String checkCart(CartInfo cartInfo, HttpServletRequest request, HttpServletResponse response, ModelMap map) {
       //  远程http的rest风格调用代码
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = new ArrayList<>();
        // 判断用户是否登录
        if(StringUtils.isNotBlank(userId)) {
          //  从缓存中查询购物车对象
            cartList = cartService.cartListFromCache(userId);
        } else {
            // 从cookie中获取购物车信息
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            cartList = JSON.parseArray(listCartCookie, CartInfo.class);
        }
        // 修改购物车状态
        for (CartInfo info : cartList) {
            if (info.getSkuId().equals(cartInfo.getSkuId())) {
                info.setIsChecked(cartInfo.getIsChecked());
                if(StringUtils.isNotBlank(userId)) {
                    cartService.updateCart(info);
                    // 刷新缓存
                    cartService.flushCartCacheByUser(userId);
                } else {
                    // 覆盖cookie
                    CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(cartList),
                            60 * 60 * 24, true);
                }
            }
        }
        map.put("cartList", cartList);
        BigDecimal b = getMySum(cartList);
        map.put("totalPrice", b);
        return "cartListInner";
    }

    @LoginRequired(isNeedLogin = false)
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request,
                           ModelMap map) {
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = new ArrayList<>();
        if (StringUtils.isNotBlank(userId)) {
             cartList = cartService.cartListFromCache(userId);
        }else {
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            if(StringUtils.isNotBlank(listCartCookie)) {
                cartList = JSON.parseArray(listCartCookie, CartInfo.class);
            }
        }

        map.put("cartList", cartList);
        BigDecimal b = getMySum(cartList);
        map.put("totalPrice", b);
        return "cartList";
    }

    private BigDecimal getMySum(List<CartInfo> cartList) {
        BigDecimal b = new BigDecimal("0");
        for (CartInfo cartInfo : cartList) {
            String isChecked = cartInfo.getIsChecked();
            if(isChecked.equals("1")) {
                b = b.add(cartInfo.getCartPrice());
            }
        }
        return b;
    }

    @LoginRequired(isNeedLogin = false)
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, int num) {
        String userId = (String) request.getAttribute("userId");
        SkuInfo skuInfo = skuService.getSkuById(skuId);
        // 创建购物车对象，并设置属性
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setSkuPrice(skuInfo.getPrice());
        cartInfo.setSkuNum(num);
        cartInfo.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(num)));
        if (StringUtils.isNotBlank(userId)) {
            cartInfo.setUserId(userId);
        }
        cartInfo.setIsChecked("1");
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setSkuName(skuInfo.getSkuName());
        List<CartInfo> cartInfos = new ArrayList<>();
        // 判断用户是否登录
        if (StringUtils.isBlank(userId)) {
            // 用户没有登录
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            cartInfos = JSON.parseArray(listCartCookie, CartInfo.class);
            if (StringUtils.isBlank(listCartCookie)) {
                cartInfos = new ArrayList<>();
                // 直接添加cookie购物车
                cartInfos.add(cartInfo);
            } else {
                // 判断是否重复
                boolean b = if_new_cart(cartInfos, cartInfo);
                if (b) {
                    // 新车
                    cartInfos.add(cartInfo);
                } else {
                    // 老车
                    for (CartInfo info : cartInfos) {
                        if (info.getSkuId().equals(skuId)) {
                            info.setSkuNum(info.getSkuNum() + cartInfo.getSkuNum());
                            info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                        }
                    }
                }
            }
            // 重新覆盖浏览器cookie
            CookieUtil.setCookie(request, response, "listCartCookie", JSON.toJSONString(cartInfos),60*60*24,true);
        } else {
            // 用户已经登录
            CartInfo exists = new CartInfo();
            exists.setUserId(userId);
            exists.setSkuId(skuId);
            CartInfo ifCart = cartService.exists(exists);
            if(ifCart == null) {
                // 添加
                cartService.saveCart(cartInfo);
            } else {
                // 修改
                ifCart.setSkuNum(ifCart.getSkuNum() + num);
                ifCart.setCartPrice(ifCart.getSkuPrice().multiply(new BigDecimal(ifCart.getSkuNum())));
                cartService.updateCart(ifCart);
            }
            // 同步缓存
            cartService.flushCartCacheByUser(userId);
        }
        return "redirect:http://cart.gmall.com:8084/success.html";
    }

    private boolean if_new_cart(List<CartInfo> cartInfos, CartInfo cartInfo) {
        boolean b = true;
        for (CartInfo info : cartInfos) {
            String skuId = info.getSkuId();
            if(skuId.equals(cartInfo.getSkuId())) {
                b = false;
            }
        }
        return b;
    }

}
