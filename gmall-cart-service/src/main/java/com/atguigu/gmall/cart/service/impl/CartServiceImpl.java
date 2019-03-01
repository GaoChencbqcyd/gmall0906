package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-23 19:13
 */
@Service
public class CartServiceImpl implements CartService{

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<CartInfo> cartListFromCache(String userId) {
        List<CartInfo> cartInfos = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals("cart:" + userId + ":info");
        for (String hval : hvals) {
            CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
            cartInfos.add(cartInfo);
        }
        return cartInfos;
    }

    @Override
    public void updateCart(CartInfo info) {
        Example example = new Example(CartInfo.class);
        example.createCriteria()
                .andEqualTo("userId",info.getUserId()).andEqualTo("skuId",info.getSkuId());
        cartInfoMapper.updateByExampleSelective(info, example);
    }

    @Override
    public void flushCartCacheByUser(String userId) {
        List<CartInfo> cartInfos = getCartInfosByUserId(userId);
        Jedis jedis = redisUtil.getJedis();
        if(cartInfos != null) {
            HashMap<String, String> stringHashMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfos) {
                stringHashMap.put(cartInfo.getId(), JSON.toJSONString(cartInfo));
            }
            jedis.hmset("cart:"+userId+":info", stringHashMap);
        }
        jedis.close();
    }

    @Override
    public CartInfo exists(CartInfo exists) {
        CartInfo cartInfo = cartInfoMapper.selectOne(exists);
        return cartInfo;
    }

    @Override
    public void saveCart(CartInfo cartInfo) {
        cartInfoMapper.insertSelective(cartInfo);
    }

    @Override
    public void mergCart(String userId, String listCartCookie) {
        List<CartInfo> cartInfosFromDb = getCartInfosByUserId(userId);
        List<CartInfo> cartInfosFromCookie = JSON.parseArray(listCartCookie, CartInfo.class);
        for (CartInfo cartInfo : cartInfosFromCookie) {
            // 判断cookie中的数据是否在db存在
            boolean b = if_new_cart(cartInfosFromDb, cartInfo);
            if(b) {
                // 不存在就插入
                cartInfo.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfo);
            }else {
                // 存在就更新
                for (CartInfo info : cartInfosFromDb) {
                    if(cartInfo.getSkuId().equals(cartInfo.getSkuId())) {
                        info.setSkuNum(info.getSkuNum() + cartInfo.getSkuNum());
                        info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                        cartInfoMapper.updateByPrimaryKeySelective(info);
                    }
                }
            }
        }
        // 同步缓存
        flushCartCacheByUser(userId);
    }

    private boolean if_new_cart(List<CartInfo> cartInfosFromDb, CartInfo cartInfo) {
        boolean b = true;
        for (CartInfo info : cartInfosFromDb) {
            String skuId = info.getSkuId();
            if(skuId.equals(cartInfo.getSkuId())){
                b = false;
            }
        }
        return b;
    }

    private List<CartInfo> getCartInfosByUserId(String userId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfos = cartInfoMapper.select(cartInfo);
        return cartInfos;
    }
}
