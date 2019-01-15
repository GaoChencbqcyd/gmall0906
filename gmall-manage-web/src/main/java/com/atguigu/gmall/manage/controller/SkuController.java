package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-15 18:19
 */
@Controller
public class SkuController {

    @Reference
    private SkuService skuService;

    @RequestMapping("saveSku")
    @ResponseBody
    public String saveSku(SkuInfo skuInfo) {
        return "success";
    }

    @RequestMapping("skuInfoListBySpu")
    @ResponseBody
    public List<SkuInfo> getSkuInfoListBySpu(String spuId) {
        List<SkuInfo> skuInfos = skuService.getSkuInfoListBySpu(spuId);
        return skuInfos;
    }
}
