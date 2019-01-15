package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;

import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-11 18:46
 */
public interface SpuService {
    List<SpuInfo> getSpuList(String catalog3Id);

    List<BaseSaleAttr> getBaseSaleAttrList();

    void saveSpu(SpuInfo spuInfo);

    List<SpuImage> getSpuImageList(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);
}
