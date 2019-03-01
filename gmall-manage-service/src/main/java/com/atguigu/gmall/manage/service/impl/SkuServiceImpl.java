package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuImage;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.manage.mapper.SkuImageMapper;
import com.atguigu.gmall.manage.mapper.SkuInfoMapper;
import com.atguigu.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-15 18:51
 */
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Override
    public List<SkuInfo> getSkuInfoListBySpu(String spuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setSpuId(spuId);
        List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfo);
        return skuInfos;
    }

    @Override
    public SkuInfo item(String skuId) {
        // 从缓存中取出sku数据
        Jedis jedis = redisUtil.getJedis();
        String skuInfoStr = jedis.get("sku:" + skuId + ":info");
        SkuInfo skuInfo = JSON.parseObject(skuInfoStr,SkuInfo.class);
        // 从db中取出sku的数据
        if(skuInfo == null) {
            // 拿到分布式锁
            String OK = jedis.set("sku:" + skuId + ":lock", "1", "nx", "px", 10000);
            if(StringUtils.isBlank(OK)) {
                return item(skuId);
            }else {
                // 拿到缓存锁，可以访问数据库
                skuInfo = itemFromDb(skuId);
            }
            jedis.del("sku:"+skuId+":lock");
            // 同步缓存一份
            jedis.set("sku:"+skuId+":info",JSON.toJSONString(skuInfo));
        }
        jedis.close();
        return skuInfo;
    }

    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

    @Override
    public SkuInfo itemFromDb(String skuId) {
        // 从db中取出sku的数据
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        SkuInfo info = skuInfoMapper.selectOne(skuInfo);

        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImages = skuImageMapper.select(skuImage);
        info.setSkuImageList(skuImages);
        return info;
    }

    @Override
    public SkuInfo getSkuById(String skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo = skuInfoMapper.selectOne(skuInfo);
        return skuInfo;
    }


}
