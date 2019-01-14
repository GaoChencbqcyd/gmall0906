package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.manage.util.ManageUploadUtil;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-11 18:45
 */
@Controller
public class SpuController {

    @Reference
    private SpuService spuService;

    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {
        String imgUrl = ManageUploadUtil.imgUpload(multipartFile);
        return imgUrl;
    }

    /**
     * 查询销售属性
     * @return
     */
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = spuService.getBaseSaleAttrList();
        return baseSaleAttrList;
    }

    @RequestMapping("saveSpu")
    @ResponseBody
    public String saveSpu(SpuInfo spuInfo) {
        spuService.saveSpu(spuInfo);
        return "success";
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> getSpuList(String catalog3Id) {
        return spuService.getSpuList(catalog3Id);
    }
}
