package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParam;

import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-21 11:59
 */
public interface ListService {
    List<SkuLsInfo> getList(SkuLsParam skuLsParam);


}
