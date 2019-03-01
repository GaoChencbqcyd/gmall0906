package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseCatalog1;
import com.atguigu.gmall.bean.BaseCatalog2;
import com.atguigu.gmall.bean.BaseCatalog3;

import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-09 18:26
 */
public interface AttrService {
     List<BaseCatalog1> getCatalog1();

     List<BaseCatalog2> getCatalog2(String catalog1Id);

     List<BaseCatalog3> getCatalog3(String catalog2Id);

     List<BaseAttrInfo> getAttrList(String catalog3Id);

     void saveAttr(BaseAttrInfo baseAttrInfo);

     List<BaseAttrInfo> getAttrListByValueIds(String join);
}
