package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-16 11:32
 */
@Controller
public class ItemController {
   @Reference
   private SkuService skuService;

   @Reference
    private SpuService spuService;

   @RequestMapping("{skuId}.html")
    public String item(@PathVariable("skuId") String skuId, ModelMap map) {
       SkuInfo skuInfo = skuService.item(skuId);
       String spuId = skuInfo.getSpuId();
       // 销售属性的集合
       List<SpuSaleAttr> spuSaleAttrListCheckBySku = spuService.getSpuSaleAttrListCheckBySku(spuId, skuId);
       map.put("spuSaleAttrListCheckBySku", spuSaleAttrListCheckBySku);
       map.put("skuInfo", skuInfo);
       // 隐藏一个hash表
      List<SkuInfo> skuInfos = skuService.getSkuSaleAttrValueListBySpu(spuId);
      HashMap<String, String> skuMap = new HashMap<>();
      for(SkuInfo sku : skuInfos) {
         String v = sku.getId();
         List<SkuSaleAttrValue> skuSaleAttrValueList = sku.getSkuSaleAttrValueList();
         String k = "";
         for(SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            String saleAttrValueId = skuSaleAttrValue.getSaleAttrValueId();
            k += ("|" + saleAttrValueId);
         }
         skuMap.put(k, v);
      }
      map.put("skuMap", JSON.toJSONString(skuMap));
      return "item";
   }


}
