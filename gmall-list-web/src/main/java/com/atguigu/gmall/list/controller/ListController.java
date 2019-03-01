package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/**
 * @author gaochen
 * @create 2019-01-21 11:35
 */
@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private AttrService attrService;

    @RequestMapping("list.html")
    public String list(SkuLsParam skuLsParam, ModelMap map) {
        // 调用list查询服务
        List<SkuLsInfo> skuLsInfoList = listService.getList(skuLsParam);
        Set<String> valueIds = new HashSet<>();
        for (SkuLsInfo skuLsInfo : skuLsInfoList) {
            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
                String valueId = skuLsAttrValue.getValueId();
                valueIds.add(valueId);
            }
        }
        // 查询检索结果列表中包含的分类属性集合
        String join = StringUtils.join(valueIds, ",");
        List<BaseAttrInfo> attrList = new ArrayList<>();
        attrList = attrService.getAttrListByValueIds(join);
        map.put("skuLsInfoList", skuLsInfoList);
        // 去掉提交的属性
        String[] valueId = skuLsParam.getValueId();
        List<AttrValueCrumb> attrValueCrumbs = new ArrayList<>();
        if (valueId != null && valueId.length > 0) {
            Iterator<BaseAttrInfo> iterator = attrList.iterator();
            while(iterator.hasNext()) {
                 List<BaseAttrValue> attrValueList = iterator.next().getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    // 列表中的属性id
                    String id = baseAttrValue.getId();
                    for(String sid : valueId) {
                        // 如果sid与id匹配，则去掉id所在的属性
                        if(id.equals(sid)) {
                            AttrValueCrumb attrValueCrumb = new AttrValueCrumb();
                            String myCrumbUrlParam = getMyCrumbUrlParam(skuLsParam, sid);
                            attrValueCrumb.setUrlParam(myCrumbUrlParam);
                            attrValueCrumb.setValueName(baseAttrValue.getValueName());
                            attrValueCrumbs.add(attrValueCrumb);
                            iterator.remove();
                        }
                    }
                }
            }
        }
        map.put("attrValueSelectedList", attrValueCrumbs);
        map.put("attrList", attrList);
        // 根据当前请求参数对象 ，生成当前请求参数字符串
        String urlParam = getMyUrlParam(skuLsParam);
        map.put("urlParam", urlParam);
        return "list";
    }

    private String getMyCrumbUrlParam(SkuLsParam skuLsParam, String... crumbValueId) {
        String urlParam = "";
        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();
        String[] valueId = skuLsParam.getValueId();
        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" +catalog3Id;
        }
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" +keyword;
        }
        if(valueId!=null&&valueId.length>0){
            for (String id : valueId) {
                if(crumbValueId!=null&&!crumbValueId[0].equals(id)){
                    urlParam = urlParam + "&valueId=" +id;
                }
            }
        }
        return urlParam;
    }

    private String getMyUrlParam(SkuLsParam skuLsParam) {
        String urlParam = "";
        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();
        String[] valueId = skuLsParam.getValueId();

        if(StringUtils.isNoneBlank(catalog3Id)) {
            if(StringUtils.isNotBlank(urlParam)) {
                urlParam += "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if(StringUtils.isNotBlank(keyword)) {
            if(StringUtils.isNotBlank(urlParam)) {
                urlParam += "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        if(valueId != null && valueId.length > 0) {
            for (String id : valueId) {
                urlParam = urlParam + "&valueId=" + id;
            }
        }
        return urlParam;
    }
}
