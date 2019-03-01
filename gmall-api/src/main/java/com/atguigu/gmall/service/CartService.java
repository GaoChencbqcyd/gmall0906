package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-23 19:13
 */
public interface CartService {
    List<CartInfo> cartListFromCache(String userId);

    void updateCart(CartInfo info);

    void flushCartCacheByUser(String userId);

    CartInfo exists(CartInfo exists);

    void saveCart(CartInfo cartInfo);

    void mergCart(String id, String listCartCookie);
}
