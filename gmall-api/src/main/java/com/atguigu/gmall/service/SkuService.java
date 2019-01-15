package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuInfo;

import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-15 18:22
 */
public interface SkuService {
    List<SkuInfo> getSkuInfoListBySpu(String spuId);
}
