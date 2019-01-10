package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-08 19:00
 */
@Controller
public class UserInfoController {
    @Reference
    private UserInfoService userInfoService;

    @RequestMapping("/user/info/list")
    @ResponseBody
    public List<UserInfo> userInfoList() {
        return userInfoService.getUserInfoList();
    }


}
