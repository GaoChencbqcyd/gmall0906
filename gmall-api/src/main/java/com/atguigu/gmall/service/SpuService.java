package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuInfo;

import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-11 18:46
 */
public interface SpuService {
    List<SpuInfo> getSpuList(String catalog3Id);

    List<BaseSaleAttr> getBaseSaleAttrList();

    void saveSpu(SpuInfo spuInfo);
}
