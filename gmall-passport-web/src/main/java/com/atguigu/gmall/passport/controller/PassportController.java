package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.manage.util.MD5Utils;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gaochen
 * @create 2019-01-26 11:30
 */
@Controller
public class PassportController {

    @Reference
    private CartService cartService;

//    @Reference
 //   private UserInfoService userInfoService;

    @Reference
    private UserService userService;

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request, String requestId,String token, ModelMap map) {
        Map userMap = JwtUtil.decode("gmall0906key", token, MD5Utils.md5(requestId));
        if(userMap != null) {
            return "success";
        } else {
            return "fail";
        }
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response, ModelMap map) {
        // 用户名和密码进行验证
        //UserInfo userLogin = userInfoService.login(userInfo);
        UserInfo userLogin = userService.login(userInfo);
        String token = "";
        if(userLogin == null) {
           // 用户名密码错误
           return "err";
        } else {
            // 生成token
            HashMap<String, String> stringHashMap = new HashMap<>();
            stringHashMap.put("userId", userLogin.getId());
            stringHashMap.put("nickName", userLogin.getNickName());

            String nip = request.getHeader("request-forwarded-for");
            if(StringUtils.isBlank(nip)) {
                nip = request.getRemoteAddr();
                if(StringUtils.isBlank(nip)) {
                    nip = "127.0.0.1";
                }
            }
            token = JwtUtil.encode("gmall0906key", stringHashMap, MD5Utils.md5(nip));
            // 将用户数据放入缓存
//            userInfoService.addUserCache(userLogin);
            userService.addUserCache(userLogin);
            // 同步购物车数据
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            if(StringUtils.isNotBlank(listCartCookie)) {
                // cookie中有购物车数据
                cartService.mergCart(userLogin.getId(),listCartCookie);
            }else {
                // cookie中没有购物车数据
                cartService.flushCartCacheByUser(userLogin.getId());
            }
            // 删除cookie中的数据
            CookieUtil.deleteCookie(request,response,"listCartCookie");
        }
        return token;
    }

    @RequestMapping("index")
    public String index(String returnUrl, ModelMap map) {
        map.put("originUrl", returnUrl);
        return "index";
    }
}
