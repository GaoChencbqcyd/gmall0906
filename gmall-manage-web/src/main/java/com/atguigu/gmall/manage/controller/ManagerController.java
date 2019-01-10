package com.atguigu.gmall.manage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author gaochen
 * @create 2019-01-09 11:59
 */
@Controller
public class ManagerController {

    @RequestMapping("index")
    public String toIndex() {
        return "index";
    }

    @RequestMapping("attrListPage")
    public String toAttrListPage() {
        return "attrListPage";
    }

}
