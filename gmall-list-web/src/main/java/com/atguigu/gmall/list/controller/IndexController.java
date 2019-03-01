package com.atguigu.gmall.list.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author gaochen
 * @create 2019-01-21 11:36
 */
@Controller
public class IndexController {

    @RequestMapping("index")
    public String index() {
        return "index";
    }
}
